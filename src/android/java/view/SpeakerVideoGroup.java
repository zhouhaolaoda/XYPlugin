/**
 * Copyright (C) 2019 XYLink Android SDK Source Project
 * <p>
 * Created by wanghui on 2019/1/22.
 */
package com.xylink.sdk.sample.view;

import android.content.Context;
import android.log.L;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ainemo.module.call.data.Enums;
import com.ainemo.sdk.model.BaseMessage;
import com.ainemo.sdk.model.PageListMessage;
import com.ainemo.sdk.model.WhiteBoardOpMessage;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.VideoInfo;
import com.ainemo.util.JsonUtil;
import com.xylink.sdk.sample.share.whiteboard.view.WhiteBoardCell;
import com.xylink.sdk.sample.share.whiteboard.view.WhiteBoardTextureView;

import java.util.ArrayList;
import java.util.List;

/**
 * 演讲模式视频布局视图类（1大屏+N小屏）
 */
public class SpeakerVideoGroup extends VideoCellGroup implements WhiteBoardTextureView.WhiteBoardViewListener,
        WhiteBoardCell.OnWhiteBoardCellEventListener {
    private static final String TAG = "SpeakerVideoGroup";
    private static final int THUMBNAIL_H = 10;
    private static final int THUMBNAIL_W = 16;
    private static final int RATIO_HEIGHT = 9;
    private static final int RATIO_WIDTH = 16;
    private static final int THUMBNAIL_CELL_NUM = 5;

    /**
     * 锁定参会者ID
     */
    private int lockedPid;

    /**
     * 内容共享者ID
     */
    private int contentPid;

    /**
     * 当前讲话者ID
     */
    private int activeSpeakerPid;

    /**
     * 是否显示画中画(小窗口)
     */
    private boolean isShowingPip = true;

    /**
     * 是否显示白板
     */
    private static boolean isShowingWhiteboard;

    /**
     * 白板
     */
    private WhiteBoardCell mWhiteBoardCell;

    /**
     * 拖动位置
     */
    private CachedLayoutPosition cachedPosition = new CachedLayoutPosition(0, 0, 0, 0);

    /**
     * 是否有拖动
     */
    private boolean hasDraged;

    public SpeakerVideoGroup(Context context) {
        super(context);
    }

    public SpeakerVideoGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeakerVideoGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        mWhiteBoardCell = new WhiteBoardCell(getContext());
        mWhiteBoardCell.setWhiteBoardListener(this);
        mWhiteBoardCell.setOnWhiteBoardCellEventListener(this);
    }

    public void addWhiteboardView() {
        ViewGroup parent = (ViewGroup) mWhiteBoardCell.getParent();
        if (null != parent) {
            parent.removeView(mWhiteBoardCell);
        }
        addView(mWhiteBoardCell.getCellLayout());
        mWhiteBoardCell.getCellLayout().setVisibility(View.GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 交换小屏和大屏位置
     *
     * @param pid
     */
    private void swapThumbnail2FullScreen(int pid) {
        if (hasDraged) {
            for (VideoCell cell : mRemoteVideoCells) {
                if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == pid) {
                    if (mFullScreenVideoCell != null) {
                        mFullScreenVideoCell.setDraged(true);

                        if (cell.isDraged()) {
                            mFullScreenVideoCell.setDragLeft(cell.getDragLeft());
                            mFullScreenVideoCell.setDragTop(cell.getDragTop());
                        } else {
                            mFullScreenVideoCell.setDragLeft(cell.getLeft());
                            mFullScreenVideoCell.setDragTop(cell.getTop());
                        }

                        break;
                    }
                }
            }

            for (VideoCell cell : mRemoteVideoCells) {
                cell.setHasOrdered(true);
                if (!cell.isDraged()) {
                    cell.setDraged(true);
                    cell.setDragLeft(cell.getLeft());
                    cell.setDragTop(cell.getTop());
                }
            }
        }

    }

    /**
     * 锁定布局
     *
     * @param pid
     */
    public void lockLayout(int pid) {
        L.i(TAG, "lockLayout, lockedPid : " + pid + "lockPidDef: " + lockedPid);
        if (lockedPid != pid) {
            lockedPid = pid;
            swapThumbnail2FullScreen(pid);
            postDelayed(mLayoutRunnabler, 200);
        }
    }

    /**
     * 解锁布局
     */
    public void unlockLayout() {
        L.i(TAG, "unlockLayout");
        if (lockedPid != 0) {
            lockedPid = 0;
            resetPip();
            postDelayed(mLayoutRunnabler, 200);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWhiteBoardCell.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mLandscape && (getWidth() > getHeight())) {
            layoutLandscape(l, t, r, b);
        } else if (!mLandscape && (getHeight() > getWidth())) {
            layoutPortrait(l, t, r, b);
        }
    }

    private void layoutLandscape(int l, int t, int r, int b) {
        computeVideoCellOrders();
        layoutFullScreenVideoCell(l, t, r, b);
        layoutThumbnailVideoCell(l, t, r, b);
        checkPip();
    }

    private void layoutPortrait(int l, int t, int r, int b) {
        computeVideoCellOrders();
        switch (mRemoteVideoCells.size()) {
            case 1:
                layoutFullScreenVideoCell(l, t, r, b);
                break;
            case 2:
                layoutTwoCells(l, t, r, b);
                break;
            case 3:
                layoutThreeCells(l, t, r, b);
                break;
            case 4:
                layoutFourCells(l, t, r, b);
                break;
            case 5:
            case 6:
                layoutFiveCells(l, t, r, b);
                break;
            default:
                break;
        }
    }

    private void layoutTwoCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() == 2) {

            mRemoteVideoCells.get(0).setLargeScreen(true);
            mRemoteVideoCells.get(0).setFullScreen(true);
            mRemoteVideoCells.get(0).setRectVisible(false);
            mRemoteVideoCells.get(0).setVisibility(VISIBLE);

            VideoInfo layoutInfo = mRemoteVideoCells.get(0).getLayoutInfo();

            if (layoutInfo != null && layoutInfo.isContent()) {
                L.i(TAG, "full.cell.info : " + layoutInfo);
                mRemoteVideoCells.get(0).layout(l, t, r, b);
            } else if (layoutInfo != null && layoutInfo.getVideoWidth() > layoutInfo.getVideoHeight()
                    || NemoSDK.getLocalVideoStreamID().equals(layoutInfo.getDataSourceID())
                    || !layoutInfo.getRemoteID().contains(Enums.DEVICE_TYPE_SOFT)) {
                L.i(TAG, "layout.getRemoteID: " + layoutInfo.getRemoteID());
                int height = (r - l) * RATIO_HEIGHT / RATIO_WIDTH;
                int start = ((b - t) - height) / 2;
                mRemoteVideoCells.get(0).layout(l, start, r, start + height);
            } else {
                int height = (r - l) * 4 / 3;
                mRemoteVideoCells.get(0).layout(l, t, r, b);
            }

            //获取新的大屏
            mFullScreenVideoCell = mRemoteVideoCells.get(0);
            if (onVideoCellListener != null) {
                onVideoCellListener.onFullScreenChanged(mFullScreenVideoCell);
            }

            L.i(TAG, "remote id: " + mRemoteVideoCells.get(1).getId());
            if (mRemoteVideoCells.get(1).getId() == VideoCell.LOCAL_VIEW_ID) {
                int cellHeight = ((b - t) - (THUMBNAIL_CELL_NUM + 1) * mCellPadding) / THUMBNAIL_CELL_NUM;
                int cellWidth = cellHeight * THUMBNAIL_H / THUMBNAIL_W;

                int left = r - l - mCellPadding - cellWidth;
                int top = mCellPadding;
                int right = r - l - mCellPadding;
                int bottom = t + mCellPadding + cellHeight;

                mRemoteVideoCells.get(1).setLargeScreen(false);
                mRemoteVideoCells.get(1).setFullScreen(false);
                mRemoteVideoCells.get(1).setRectVisible(true);
                mRemoteVideoCells.get(1).setVisibility(VISIBLE);
                mRemoteVideoCells.get(1).bringToFront();
                if (hasDraged) {
                    if (mRemoteVideoCells.get(1).isDraged()) {
                        int dragLeft = mRemoteVideoCells.get(1).getDragLeft();
                        int dragTop = mRemoteVideoCells.get(1).getDragTop();
                        if (dragLeft + cellWidth > r) {
                            dragLeft = r - cellWidth;
                        }
                        if (dragTop + cellHeight > b) {
                            dragTop = b - cellHeight;
                        }
                        mRemoteVideoCells.get(1).layout(dragLeft, dragTop, dragLeft + cellWidth, dragTop + cellHeight);
                    } else {
                        mRemoteVideoCells.get(1).layout(cachedPosition.getL(), cachedPosition.getT(), cachedPosition.getR(), cachedPosition.getB());
                    }
                } else {
                    mRemoteVideoCells.get(1).layout(left, top, right, bottom);
                }
            } else {
                int cellWidth;
                int cellHeight;
                if(mRemoteVideoCells.get(1).getLayoutInfo() != null && mRemoteVideoCells.get(1).getLayoutInfo().getVideoWidth() > mRemoteVideoCells.get(1).getLayoutInfo().getVideoHeight()
                        || mRemoteVideoCells.get(1).getLayoutInfo() != null && !layoutInfo.getRemoteID().contains(Enums.DEVICE_TYPE_SOFT)) {
                    cellWidth = ((b - t) - (THUMBNAIL_CELL_NUM + 1) * mCellPadding) / THUMBNAIL_CELL_NUM;
                    cellHeight = cellWidth * THUMBNAIL_H / THUMBNAIL_W;
                } else {
                    cellHeight = ((b - t) - (THUMBNAIL_CELL_NUM + 1) * mCellPadding) / THUMBNAIL_CELL_NUM;
                    cellWidth = cellHeight * THUMBNAIL_H / THUMBNAIL_W;
                }

                int left = r - l - mCellPadding - cellWidth;
                int top = mCellPadding;
                int right = r - l - mCellPadding;
                int bottom = t + mCellPadding + cellHeight;

                mRemoteVideoCells.get(1).setLargeScreen(false);
                mRemoteVideoCells.get(1).setFullScreen(false);
                mRemoteVideoCells.get(1).setRectVisible(true);
                mRemoteVideoCells.get(1).setVisibility(VISIBLE);
                mRemoteVideoCells.get(1).bringToFront();
                if (hasDraged) {
                    if (mRemoteVideoCells.get(1).isDraged()) {
                        int dragLeft = mRemoteVideoCells.get(1).getDragLeft();
                        int dragTop = mRemoteVideoCells.get(1).getDragTop();
                        if (dragLeft + cellWidth > r) {
                            dragLeft = r - cellWidth;
                        }
                        if (dragTop + cellHeight > b) {
                            dragTop = b - cellHeight;
                        }
                        mRemoteVideoCells.get(1).layout(dragLeft, dragTop, dragLeft + cellWidth, dragTop + cellHeight);
                    } else {
                        mRemoteVideoCells.get(1).layout(cachedPosition.getL(), cachedPosition.getT(), cachedPosition.getR(), cachedPosition.getB());
                    }
                } else {
                    mRemoteVideoCells.get(1).layout(left, top, right, bottom);
                }
            }
        }
    }

    private void layoutThreeCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() == 3) {
            int cellWidth = (r - l) / 2;
            int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

            int left = 0, top = 0, right = 0, bottom = 0;

            left = l;
            top = ((b - t) - (cellWidth + cellHeight)) / 2;
            right = r;
            bottom = top + 2 * cellHeight;

            mRemoteVideoCells.get(0).setLargeScreen(true);
            mRemoteVideoCells.get(0).setFullScreen(false);
            mRemoteVideoCells.get(0).setRectVisible(false);
            mRemoteVideoCells.get(0).setVisibility(VISIBLE);
            mRemoteVideoCells.get(0).layout(left, top, right, bottom);

            //获取新的大屏
            mFullScreenVideoCell = mRemoteVideoCells.get(0);
            if (onVideoCellListener != null) {
                onVideoCellListener.onFullScreenChanged(mFullScreenVideoCell);
            }

            left = l;
            top = bottom;
            right = cellWidth;
            bottom = top + cellHeight;

            mRemoteVideoCells.get(1).setLargeScreen(false);
            mRemoteVideoCells.get(1).setFullScreen(false);
            mRemoteVideoCells.get(1).setRectVisible(false);
            mRemoteVideoCells.get(1).setVisibility(VISIBLE);
            mRemoteVideoCells.get(1).layout(left, top, right, bottom);

            left = right;
            right = r;
            mRemoteVideoCells.get(2).setLargeScreen(false);
            mRemoteVideoCells.get(2).setFullScreen(false);
            mRemoteVideoCells.get(2).setRectVisible(false);
            mRemoteVideoCells.get(2).setVisibility(VISIBLE);
            mRemoteVideoCells.get(2).layout(left, top, right, bottom);

        }
    }

    private void layoutFourCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() == 4) {
            int cellWidth = (r - l) / 2;
            int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

            int left = 0, top = 0, right = 0, bottom = 0;

            left = l;
            top = ((b - t) - (cellWidth + cellHeight * 2)) / 2;
            right = r;
            bottom = top + 2 * cellHeight;

            mRemoteVideoCells.get(0).setLargeScreen(true);
            mRemoteVideoCells.get(0).setFullScreen(false);
            mRemoteVideoCells.get(0).setRectVisible(false);
            mRemoteVideoCells.get(0).setVisibility(VISIBLE);
            mRemoteVideoCells.get(0).layout(left, top, right, bottom);

            //获取新的大屏
            mFullScreenVideoCell = mRemoteVideoCells.get(0);
            if (onVideoCellListener != null) {
                onVideoCellListener.onFullScreenChanged(mFullScreenVideoCell);
            }

            left = l;
            top = bottom;
            right = cellWidth;
            bottom = top + cellHeight;

            mRemoteVideoCells.get(1).setLargeScreen(false);
            mRemoteVideoCells.get(1).setFullScreen(false);
            mRemoteVideoCells.get(1).setRectVisible(false);
            mRemoteVideoCells.get(1).setVisibility(VISIBLE);
            mRemoteVideoCells.get(1).layout(left, top, right, bottom);

            left = right;
            right = r;
            mRemoteVideoCells.get(2).setLargeScreen(false);
            mRemoteVideoCells.get(2).setFullScreen(false);
            mRemoteVideoCells.get(2).setRectVisible(false);
            mRemoteVideoCells.get(2).setVisibility(VISIBLE);
            mRemoteVideoCells.get(2).layout(left, top, right, bottom);

            left = l;
            top = bottom;
            right = cellWidth;
            bottom = top + cellHeight;
            mRemoteVideoCells.get(3).setLargeScreen(false);
            mRemoteVideoCells.get(3).setFullScreen(false);
            mRemoteVideoCells.get(3).setRectVisible(false);
            mRemoteVideoCells.get(3).setVisibility(VISIBLE);
            mRemoteVideoCells.get(3).layout(left, top, right, bottom);
        }
    }

    private void layoutFiveCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() >= 5) {
            int cellWidth = (r - l) / 2;
            int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

            int left = 0, top = 0, right = 0, bottom = 0;

            left = l;
            top = ((b - t) - (cellWidth + cellHeight * 2)) / 2;
            right = r;
            bottom = top + 2 * cellHeight;

            mRemoteVideoCells.get(0).setLargeScreen(true);
            mRemoteVideoCells.get(0).setFullScreen(false);
            mRemoteVideoCells.get(0).setRectVisible(false);
            mRemoteVideoCells.get(0).layout(left, top, right, bottom);
            mRemoteVideoCells.get(0).setVisibility(VISIBLE);

            //获取新的大屏
            mFullScreenVideoCell = mRemoteVideoCells.get(0);
            if (onVideoCellListener != null) {
                onVideoCellListener.onFullScreenChanged(mFullScreenVideoCell);
            }

            left = l;
            top = bottom;
            right = cellWidth;
            bottom = top + cellHeight;

            mRemoteVideoCells.get(1).setLargeScreen(false);
            mRemoteVideoCells.get(1).setFullScreen(false);
            mRemoteVideoCells.get(1).setRectVisible(false);
            mRemoteVideoCells.get(1).setVisibility(VISIBLE);
            mRemoteVideoCells.get(1).layout(left, top, right, bottom);

            left = right;
            right = r;
            mRemoteVideoCells.get(2).setLargeScreen(false);
            mRemoteVideoCells.get(2).setFullScreen(false);
            mRemoteVideoCells.get(2).setRectVisible(false);
            mRemoteVideoCells.get(2).setVisibility(VISIBLE);
            mRemoteVideoCells.get(2).layout(left, top, right, bottom);

            left = l;
            top = bottom;
            right = cellWidth;
            bottom = top + cellHeight;
            mRemoteVideoCells.get(3).setLargeScreen(false);
            mRemoteVideoCells.get(3).setFullScreen(false);
            mRemoteVideoCells.get(3).setRectVisible(false);
            mRemoteVideoCells.get(3).setVisibility(VISIBLE);
            mRemoteVideoCells.get(3).layout(left, top, right, bottom);

            left = right;
            right = r;
            mRemoteVideoCells.get(4).setLargeScreen(false);
            mRemoteVideoCells.get(4).setFullScreen(false);
            mRemoteVideoCells.get(4).setRectVisible(false);
            mRemoteVideoCells.get(4).setVisibility(VISIBLE);
            mRemoteVideoCells.get(4).layout(left, top, right, bottom);
        }
    }


    private void layoutFullScreenVideoCell(int l, int t, int r, int b) {
        if (isShowingWhiteboard) {
            mWhiteBoardCell.layout(l, t, r, b);
            mWhiteBoardCell.setFullScreen(true);
            mWhiteBoardCell.bringToFront();

            mFullScreenVideoCell = null;
            if (onVideoCellListener != null) {
                onVideoCellListener.onFullScreenChanged(null);
            }
        } else {
            if (mRemoteVideoCells.size() > 0) {

                //获取新的大屏
                mFullScreenVideoCell = mRemoteVideoCells.get(0);

                mFullScreenVideoCell.setFullScreen(true);
                mFullScreenVideoCell.setRectVisible(false);
                if (mFullScreenVideoCell.getLayoutInfo() != null && mFullScreenVideoCell.getLayoutInfo().isContent()) {
                    mFullScreenVideoCell.layout(l, t, r, b);
                } else if (mFullScreenVideoCell.getLayoutInfo() != null
                        && mFullScreenVideoCell.getLayoutInfo().getVideoHeight() > mFullScreenVideoCell.getLayoutInfo().getVideoWidth()) {
                    int height = b - t;
                    int width = height * 3 / 4;

                    mFullScreenVideoCell.layout((r - l - width) / 2, t, (r - l + width) / 2, b);
                } else {
                    mFullScreenVideoCell.layout(l, t, r, b);
                }
                if (onVideoCellListener != null) {
                    onVideoCellListener.onFullScreenChanged(mFullScreenVideoCell);
                }
            }
        }

        L.i(TAG, "layoutFullScreenVideoCell, isShowingWhiteboard : " + isShowingWhiteboard + ", layoutInfo : " + (mFullScreenVideoCell != null ? mFullScreenVideoCell.getLayoutInfo() : null));
    }

    private void layoutThumbnailVideoCell(int l, int t, int r, int b) {
        int cellWidth = ((r - l) - (THUMBNAIL_CELL_NUM + 1) * mCellPadding) / THUMBNAIL_CELL_NUM;
        int cellHeight = cellWidth * THUMBNAIL_H / THUMBNAIL_W;

        if (isShowingWhiteboard) {
            if (mRemoteVideoCells.size() > 0) {
                int left = mCellPadding;
                int top = (b - t) - (cellHeight + mCellPadding);
                int right = mCellPadding + cellWidth;
                int bottom = (b - t) - mCellPadding;

                mRemoteVideoCells.get(0).setLargeScreen(false);
                mRemoteVideoCells.get(0).setFullScreen(false);
                mRemoteVideoCells.get(0).setRectVisible(true);
                if (hasDraged) {
                    if (mRemoteVideoCells.get(0).isDraged()) {
                        int dragLeft = mRemoteVideoCells.get(0).getDragLeft();
                        int dragTop = mRemoteVideoCells.get(0).getDragTop();
                        mRemoteVideoCells.get(0).layout(dragLeft, dragTop, dragLeft + cellWidth, dragTop + cellHeight);
                    } else {
                        mRemoteVideoCells.get(0).layout(cachedPosition.getL(), cachedPosition.getT(), cachedPosition.getR(), cachedPosition.getB());
                    }
                } else {
                    mRemoteVideoCells.get(0).layout(left, top, right, bottom);
                }
                mRemoteVideoCells.get(0).bringToFront();
            }
        } else {
            if (mRemoteVideoCells.size() > 1) {
                for (int i = 1; i < mRemoteVideoCells.size(); i++) {
                    int left = mCellPadding + (i - 1) * (cellWidth + mCellPadding);
                    int top = (b - t) - (cellHeight + mCellPadding);
                    int right = (mCellPadding + cellWidth) * i;
                    int bottom = (b - t) - mCellPadding;

                    mRemoteVideoCells.get(i).setLargeScreen(false);
                    mRemoteVideoCells.get(i).setFullScreen(false);
                    mRemoteVideoCells.get(i).setRectVisible(true);
                    if (hasDraged) {
                        if (mRemoteVideoCells.get(i).isDraged()) {
                            int dragLeft = mRemoteVideoCells.get(i).getDragLeft();
                            int dragTop = mRemoteVideoCells.get(i).getDragTop();
                            mRemoteVideoCells.get(i).layout(dragLeft, dragTop, dragLeft + cellWidth, dragTop + cellHeight);
                        } else {
                            if (contentPid > 0 && i == 1) {
                                mRemoteVideoCells.get(i).layout(cachedPosition.getL(), cachedPosition.getT(), cachedPosition.getR(), cachedPosition.getB());
                            } else if (!mRemoteVideoCells.get(i).hasOrdered()) {
                                mRemoteVideoCells.get(i).layout(left, top, right, bottom);
                                mRemoteVideoCells.get(i).setHasOrdered(true);
                            } else {
                                mRemoteVideoCells.get(i).layout(left, top, right, bottom);
                            }
                        }
                    } else {
                        mRemoteVideoCells.get(i).layout(left, top, right, bottom);
                    }

                    mRemoteVideoCells.get(i).bringToFront();
                }
            }
        }
    }

    private void checkPip() {
        L.i(TAG, "checkPip, isShowingPip : " + isShowingPip);

        if (isShowingWhiteboard) {
            for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                if (isShowingPip) {
                    mRemoteVideoCells.get(i).setVisibility(i == 0 ? VISIBLE : GONE);
                } else {
                    mRemoteVideoCells.get(i).setVisibility(GONE);
                }
            }
        } else {
            for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                if (isShowingPip) {
                    mRemoteVideoCells.get(i).setVisibility(VISIBLE);
                } else {
                    mRemoteVideoCells.get(i).setVisibility(i == 0 ? VISIBLE : GONE);
                }
            }
        }
    }

    private void resetPip() {
        if (hasDraged) {
            for (VideoCell cell : mRemoteVideoCells) {
                cell.setDraged(false);
                cell.setHasOrdered(false);
                cell.setDragLeft(0);
                cell.setDragTop(0);
            }
            hasDraged = false;
            cachedPosition.setVals(0, 0, 0, 0);
            postDelayed(mLayoutRunnabler, DEALY_LAYOUT);
        }
    }

    @Override
    protected void onOrientationChanged(boolean isLandscape) {
        resetPip();
    }

    /**
     * 设置是否显示画中
     *
     * @param isShowingPip
     */
    public void setShowingPip(boolean isShowingPip) {
        if (this.isShowingPip != isShowingPip) {
            this.isShowingPip = isShowingPip;
            checkPip();
        }
    }

    /**
     * 是否画中画
     *
     * @return
     */
    public boolean isShowingPip() {
        return isShowingPip;
    }

    /**
     * 是否显示白板
     *
     * @return
     */
    public static boolean isShowingWhiteboard() {
        return isShowingWhiteboard;
    }

    /**
     * 锁定屏幕
     *
     * @return
     */
    public int getLockedPid() {
        return lockedPid;
    }

    /**
     * 内容
     *
     * @return
     */
    public int getContentPid() {
        return contentPid;
    }

    /**
     * 讲话者
     *
     * @return
     */
    public int getActiveSpeakerPid() {
        return activeSpeakerPid;
    }

    @Override
    public synchronized void setRemoteVideoInfos(List<VideoInfo> infos) {
        if (infos != null && infos.size() > 0) {
            mRemoteVideoInfos = infos;

            L.i(TAG, "video info: " + infos.get(0).getVideoWidth() + " " + infos.get(0).getVideoHeight());

            //check the active speaker & content ID
            computeActiveAndContentPid(infos);

            L.i(TAG, "setRemoteVideoInfos, mRemoteVideoInfos.size : " + mRemoteVideoInfos.size() + ", mRemoteVideoCells.size : " + mRemoteVideoCells.size());

            if (mRemoteVideoCells.size() > 0) {
                //1.compute none existed video cell
                List<VideoCell> toDel = new ArrayList<>();

                for (VideoCell cell : mRemoteVideoCells) {
                    for (int i = 0; i < mRemoteVideoInfos.size(); i++) {
                        VideoInfo info = mRemoteVideoInfos.get(i);
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == info.getParticipantId()) {
                            break;
                        }

                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getLayoutVideoState().equals(Enums.LAYOUT_STATE_ADDOTHER)) {
                            break;
                        }

                        if (i == mRemoteVideoInfos.size() - 1) {
                            toDel.add(cell);
                        }
                    }
                }

                L.i(TAG, "setRemoteVideoInfos, toDel.size : " + toDel.size());

                //2.delete none existed video cell
                for (VideoCell cell : toDel) {
                    if (cell.getId() != VideoCell.LOCAL_VIEW_ID) {
                        removeView(cell);
                        mRemoteVideoCells.remove(cell);
                    }
                }

                toDel.clear();
            }


            //3.update already exist cell & add new cell
            if (mRemoteVideoCells.size() > 0) {
                for (VideoInfo info : mRemoteVideoInfos) {
                    for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                        VideoCell cell = mRemoteVideoCells.get(i);

                        //update already exit cell
                        if (cell.getLayoutInfo() != null && info.getParticipantId() == cell.getLayoutInfo().getParticipantId()) {
                            android.util.Log.d(TAG, "oldInfo : " + cell.getLayoutInfo());
                            android.util.Log.d(TAG, "newInfo : " + info);
                            if (info.getLayoutVideoState().equals(Enums.LAYOUT_STATE_MUTE)
                                    || info.getLayoutVideoState().equals(Enums.LAYOUT_STATE_REQUESTING)
                                    || info.getLayoutVideoState().equals(Enums.LAYOUT_STATE_AUDIO_ONLY)) {
                                info.setVideoHeight(cell.getLayoutInfo().getVideoHeight());
                                info.setVideoWidth(cell.getLayoutInfo().getVideoWidth());
                            }
                            cell.setLayoutInfo(info);
                            break;
                        }

                        //add new cell
                        if (i == mRemoteVideoCells.size() - 1) {
                            mRemoteVideoCells.add(createRemoteCell(info, false));
                        }
                    }
                }

                //reorder video cell
                for (int i = 0; i < mRemoteVideoInfos.size(); i++) {
                    for (int j = 0; j < mRemoteVideoCells.size(); j++) {
                        VideoCell cell = mRemoteVideoCells.get(j);
                        if (cell.getLayoutInfo() != null && mRemoteVideoInfos.get(i).getParticipantId() == cell.getLayoutInfo().getParticipantId()) {
                            if (i < j) {
                                mRemoteVideoCells.remove(cell);
                                mRemoteVideoCells.add(i, cell);
                                break;
                            }
                        }
                    }
                }
            } else {
                for (VideoInfo info : mRemoteVideoInfos) {
                    mRemoteVideoCells.add(createRemoteCell(info, false));
                }
            }

            L.i(TAG, "setRemoteVideoInfos,  mRemoteVideoCells.size : " + mRemoteVideoCells.size());

            //4.relayout accordingly child count & others
            post(mLayoutRunnabler);
        } else {
            //1. clean ui element
            for (VideoCell cell : mRemoteVideoCells) {
                if (cell.getId() != VideoCell.LOCAL_VIEW_ID) {
                    removeView(cell);
                    mRemoteVideoCells.remove(cell);
                }
            }

            //2. relayout accordingly child count & others
            post(mLayoutRunnabler);
            //3. clean up data
            if (mRemoteVideoInfos != null) {
                mRemoteVideoInfos.clear();
            }
        }
    }

    /**
     * 检测讲话者和内容分享者ID
     *
     * @param infos
     */
    private void computeActiveAndContentPid(List<VideoInfo> infos) {
        L.i(TAG, "before checkActiveAndContentPid, contentPid : " + contentPid + ", activeSpeakerPid : " + activeSpeakerPid);

        //计算内容ID
        for (VideoInfo info : infos) {
            if (info.isContent()) {
                contentPid = info.getParticipantId();
                break;
            }
            contentPid = 0;
        }

        //计算讲话者ID
        for (VideoInfo info : infos) {
            if (info.isActiveSpeaker()) {
                activeSpeakerPid = info.getParticipantId();
                break;
            }
            activeSpeakerPid = 0;
        }

        L.i(TAG, "after checkActiveAndContentPid, contentPid : " + contentPid + ", activeSpeakerPid : " + activeSpeakerPid);
    }

    /**
     * 计算视频单元顺序
     */
    private synchronized void computeVideoCellOrders() {

        L.i(TAG, "computeVideoCellOrders, activeSpeakerPid : " + activeSpeakerPid + ", lockedPid : " + lockedPid + ", contentPid : " + contentPid);
        if (isShowingWhiteboard) {
            if (mRemoteVideoCells.size() >= 2) {
                //移除本地阅览和内容(白板打开时，有可能内容停止不及时，出项左下角小窗口的闪动)
                for (VideoCell cell : mRemoteVideoCells) {
                    if (cell.getId() == VideoCell.LOCAL_VIEW_ID || (cell.getLayoutInfo() != null && cell.getLayoutInfo().isContent())) {
                        mRemoteVideoCells.remove(cell);
                        removeView(cell);
                        break;
                    }
                }
                if (activeSpeakerPid > 0) {
                    //讲话者上大屏
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == activeSpeakerPid) {
                            mRemoteVideoCells.remove(cell);
                            mRemoteVideoCells.add(0, cell);
                            break;
                        }
                    }
                }
            }
        } else {
            if (contentPid > 0) {
                if (mRemoteVideoCells.size() > 2) {
                    //移除本地阅览
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getId() == VideoCell.LOCAL_VIEW_ID) {
                            mRemoteVideoCells.remove(cell);
                            removeView(cell);
                            break;
                        }
                    }

                } else if (mRemoteVideoCells.size() < 2) {
                    //添加local到窗口列表
                    if (!mRemoteVideoCells.contains(mLocalVideoCell)) {
                        addView(mLocalVideoCell);
                        mRemoteVideoCells.add(mLocalVideoCell);
                    }
                }

                if (lockedPid > 0) {
                    //锁定优先
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == lockedPid) {
                            mRemoteVideoCells.remove(cell);
                            mRemoteVideoCells.add(0, cell);
                            break;
                        }
                    }
                } else {
                    //内容优先
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == contentPid) {
                            mRemoteVideoCells.remove(cell);
                            mRemoteVideoCells.add(0, cell);
                            break;
                        }
                    }
                }
            } else {

                //添加local到窗口列表
                if (!mRemoteVideoCells.contains(mLocalVideoCell)) {
                    addView(mLocalVideoCell);
                    mRemoteVideoCells.add(mLocalVideoCell);
                }

                if (lockedPid > 0) {
                    //锁定上大屏
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == lockedPid) {
                            mRemoteVideoCells.remove(cell);
                            mRemoteVideoCells.add(0, cell);
                            break;
                        }
                    }
                } else if (activeSpeakerPid > 0) {
                    //讲话者上大屏
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == activeSpeakerPid) {
                            mRemoteVideoCells.remove(cell);
                            mRemoteVideoCells.add(0, cell);
                            break;
                        }
                    }
                }

                //设置local在底部第一位置
                if (mRemoteVideoCells.size() >= 2) {
                    for (VideoCell cell : mRemoteVideoCells) {
                        if (cell.getId() == VideoCell.LOCAL_VIEW_ID && cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() != lockedPid) {
                            mRemoteVideoCells.remove(cell);
                            mRemoteVideoCells.add(1, cell);
                            break;
                        }
                    }
                }
            }
        }

        //邀请用户的cell排到最后
        List<VideoCell> addOhters = new ArrayList<>();
        for (VideoCell cell : mRemoteVideoCells) {
            if (cell.getLayoutInfo() != null && (cell.getLayoutInfo().getLayoutVideoState().equals(Enums.LAYOUT_STATE_ADDOTHER)
                    || cell.getLayoutInfo().getLayoutVideoState() == Enums.LAYOUT_STATE_ADDOTHER_FAILED)) {
                mRemoteVideoCells.remove(cell);
                addOhters.add(cell);
            }
        }

        mRemoteVideoCells.addAll(addOhters);

        //UI just support max six channel video, such as one local and five remote
        if (mRemoteVideoCells.size() > THUMBNAIL_CELL_NUM + 1) {
            List<VideoCell> toDel = mRemoteVideoCells.subList(THUMBNAIL_CELL_NUM + 1, mRemoteVideoCells.size());

            for (VideoCell cell : toDel) {
                removeView(cell);
                mRemoteVideoCells.remove(cell);
            }
        }

        L.i(TAG, "computeVideoCellOrders, mRemoteVideoCells.size : " + mRemoteVideoCells.size());
    }

    public void onWhiteBoardMessages(String text) {
        L.i("WhiteBoard onWhiteBoardMessages::::: " + text);
        BaseMessage bm = JsonUtil.toObject(text, BaseMessage.class);
        if (null == bm) {
            return;
        }
        WhiteBoardCell cell = null;
        if (null != mWhiteBoardCell && mWhiteBoardCell instanceof WhiteBoardCell) {
            cell = mWhiteBoardCell;
        }
        if (null == cell) {
            return;
        }
        switch (bm.getType()) {
            case BaseMessage.WhiteBoardOp:
                WhiteBoardOpMessage wbOpMessage = JsonUtil.toObject(text, WhiteBoardOpMessage.class);
                if (null == wbOpMessage.getUrl()) {
                    stopWhiteboard();
                } else {
                    showWhiteboard(wbOpMessage.getProp());
                }
                break;
            case BaseMessage.PageList:
                PageListMessage plmsg = JsonUtil.toObject(text, PageListMessage.class);
                if (null != plmsg.getP() && !plmsg.getP().isEmpty()
                        && plmsg.getC() >= 0
                        && plmsg.getC() < plmsg.getP().size()) {

                    PageListMessage.Page current = plmsg.getP().get(plmsg.getC());
                    showWhiteboard(current.getProp());
                }
                break;
            case BaseMessage.LineType:
            case BaseMessage.ClearLines:
                cell.onMessage(text);
                break;
            default:
                L.i("handleTextArrival ignore: " + text);
                break;
        }
    }

    public void removeWhiteboardView() {
        if (mWhiteBoardCell != null)
            removeView(mWhiteBoardCell.getCellLayout());
    }

    public void handleWhiteboardLinesMessage(ArrayList<String> lines) {
        L.i("WhiteBoard-->handleWhiteboardLinesMessage message arrive start to draw line");
        WhiteBoardCell cell = null;
        if (null != mWhiteBoardCell && mWhiteBoardCell instanceof WhiteBoardCell) {
            cell = mWhiteBoardCell;
        }
        if (null == cell) {
            return;
        }
        cell.onMessages(lines);
    }

    private void showWhiteboard(String prop) {
        L.i(TAG, "showWhiteboard, prop : " + prop);
        if (mWhiteBoardCell != null) {
            if (prop != null) {
                mWhiteBoardCell.setWhiteBoardResolution(prop);
            }
            mWhiteBoardCell.switchToDisplayState();
            mWhiteBoardCell.show(false);
            mWhiteBoardCell.setVisibility(View.VISIBLE);
            mWhiteBoardCell.setFullScreen(true);
            mWhiteBoardCell.bringToFront();
            postDelayed(mLayoutRunnabler, DEALY_LAYOUT);
        }
    }

    private void hideWhiteboard() {
        L.i(TAG, "hideWhiteboard");
        if (mWhiteBoardCell != null) {
            if (mWhiteBoardCell.getVisibility() == View.VISIBLE) {
                mWhiteBoardCell.onPause();
                mWhiteBoardCell.setVisibility(View.GONE);
                mWhiteBoardCell.stop();
            }
            postDelayed(mLayoutRunnabler, DEALY_LAYOUT);
        }
    }

    /**
     * 开始白板
     */
    public void startWhiteboard() {
        L.i(TAG, "startWhiteboard");
        isShowingWhiteboard = true;
        addWhiteboardView();
        showWhiteboard(null);
    }

    /**
     * 停止白板
     */
    public void stopWhiteboard() {
        L.i(TAG, "stopWhiteboard");
        isShowingWhiteboard = false;
        removeWhiteboardView();
        hideWhiteboard();
    }

    private VideoCell createRemoteCell(VideoInfo layoutInfo, boolean playCreateAnimation) {
        L.i(TAG, "createRemoteCell, remoteVidoeInfo : " + layoutInfo);
        VideoCell cell = new VideoCell(playCreateAnimation, getContext(), this);
        cell.setLayoutInfo(layoutInfo);
        cell.bringToFront();

        addView(cell);
        return cell;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createLocalCell(boolean isUvc) {
        L.i(TAG, "createLocalCell, isUvc : " + isUvc);
        mLocalVideoCell = new VideoCell(isUvc, false, getContext(), this);
        mLocalVideoCell.setId(VideoCell.LOCAL_VIEW_ID);
        mLocalVideoCell.bringToFront();
        mLocalVideoCell.setLayoutInfo(mLocalVideoInfo);
    }

    public void setLocalVideoState(boolean visible) {
        if (visible) {
            mLocalVideoCell.setVisibility(VISIBLE);
        } else {
            mLocalVideoCell.setVisibility(GONE);
        }
    }

    @Override
    public void onLongPress(MotionEvent e, VideoCell cell) {
        if (cell.isFullScreen()) {
            resetPip();
        }
        super.onLongPress(e, cell);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell) {
        if (cell.isFullScreen()) {
            //全屏不允许拖动
        } else {
            if (mLandscape || (!mLandscape && mRemoteVideoCells.size() == 2)) {
                int left = cell.getLeft() + (int) distanceX;
                int top = cell.getTop() + (int) distanceY;
                int right = cell.getRight() + (int) distanceX;
                int bottom = cell.getBottom() + (int) distanceY;

                if (left < 0) {
                    left = 0;
                    right = left + cell.getWidth();
                }
                if (right > getWidth()) {
                    right = getWidth();
                    left = right - cell.getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + cell.getHeight();
                }
                if (bottom > getHeight()) {
                    bottom = getHeight();
                    top = bottom - cell.getHeight();
                }
                cell.setDraged(true);
                VideoCell.isConsumingEvent = true;
                hasDraged = true;
                cachedPosition.setVals(left, top, right, bottom);
                cell.layout(left, top, right, bottom);
                cell.setDragLeft(left);
                cell.setDragTop(top);
            }
        }
        return super.onScroll(e1, e2, distanceX, distanceY, cell);
    }

    @Override
    public void onLongPress(MotionEvent e, WhiteBoardCell cell) {
        resetPip();
        if (onVideoCellListener != null) {
            onVideoCellListener.onLongPress(e, cell);
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, WhiteBoardCell cell) {
        if (onVideoCellListener != null) {
            return onVideoCellListener.onDoubleTap(e, cell);
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, WhiteBoardCell cell) {
        L.i("wang whiteboard onSingleTapConfirmed");
        if (onVideoCellListener != null) {
            return onVideoCellListener.onSingleTapConfirmed(e, cell);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, WhiteBoardCell cell) {
        if (onVideoCellListener != null) {
            return onVideoCellListener.onScroll(e1, e2, distanceX, distanceY, cell);
        }
        return true;
    }

    @Override
    public void onWhiteBoardMessageSend(String text) {
        if (onVideoCellListener != null) {
            onVideoCellListener.onWhiteboardMessageSend(text);
        }
    }
}
