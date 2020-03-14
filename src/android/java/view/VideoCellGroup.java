package com.xylink.sdk.sample.view;

import android.content.Context;
import android.log.L;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ainemo.sdk.otf.VideoInfo;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

import com.xylink.sdk.sample.face.FaceView;
import com.xylink.sdk.sample.view.VideoCell.OnCellEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class VideoCellGroup extends ViewGroup implements VideoCellLayout,
        OnCellEventListener, View.OnClickListener {

    protected static final String TAG = VideoCellGroup.class.getSimpleName();

    protected static final long DEALY_LAYOUT = 20;

    protected OnVideoCellListener onVideoCellListener;
    private int frameRate = 15;
    protected VideoInfo mLocalVideoInfo;
    protected volatile VideoCell mLocalVideoCell;
    protected volatile List<VideoInfo> mRemoteVideoInfos;
    protected volatile List<VideoCell> mRemoteVideoCells;
    protected VideoCell mFullScreenVideoCell;
    protected int mCellPadding;
    protected int mCurrentIndex;
    protected String mChairmanUri;
    protected boolean mLandscape;

    protected Runnable mLayoutRunnabler = new Runnable() {
        @Override
        public void run() {
            //防止重复布局
            removeCallbacks(mLayoutRunnabler);
            requestLayout();
        }
    };

    protected Runnable mRenderRunnabler = new Runnable() {
        @Override
        public void run() {
            mLocalVideoCell.requestRender();
            for (VideoCell cell : mRemoteVideoCells) {
                cell.requestRender();
            }
            requestRender(true);
        }
    };

    private void requestRender(boolean isRendering) {
        removeCallbacks(mRenderRunnabler);
        if (isRendering) {
            if (getVisibility() == VISIBLE) {
                postDelayed(mRenderRunnabler, 1000 / frameRate);
            }
        }
    }

    public VideoCellGroup(Context context) {
        this(context, null);
    }

    public VideoCellGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoCellGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        this.setOnClickListener(this);
        mRemoteVideoCells = new CopyOnWriteArrayList<>();

        //设置背景色
        setBackgroundResource(R.color.video_bg);

        mCellPadding = (int) getResources().getDimension(R.dimen.local_cell_pandding);

        setClipChildren(false);

        //创建本地视频
        createLocalCell(false);
    }

    protected abstract void createLocalCell(boolean isUvc);

    @Override
    public void setMuteLocalAudio(boolean mute) {
        mLocalVideoCell.setMuteAudio(mute);
    }

    @Override
    public void setMuteLocalVideo(boolean mute, String reason) {
        mLocalVideoCell.setMuteVideo(mute, reason);
    }

    @Override
    public void setAudioOnlyMode(boolean flag, boolean isLocalVideoMute) {
        mLocalVideoCell.setAudioOnly(flag);
        if (!flag) {
            mLocalVideoCell.setMuteVideo(isLocalVideoMute, "MuteByMyself");
        }
    }

    @Override
    public void setLandscape(boolean landscape) {
        if (mLandscape != landscape) {
            mLandscape = landscape;
            removeAllFaceView();
            onOrientationChanged(landscape);
            postDelayed(mLayoutRunnabler, DEALY_LAYOUT);
        }
    }

    /**
     * 是否横屏
     *
     * @return
     */
    public boolean isLandscape() {
        return mLandscape;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        postDelayed(mLayoutRunnabler, DEALY_LAYOUT);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        L.i(TAG, "onVisibilityChanged,  visibility : " + (visibility == VISIBLE));
        requestRender(visibility == VISIBLE);
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void onClick(View v) {
        L.i(TAG, "onClick");
        if (onVideoCellListener != null) {
            onVideoCellListener.onVideoCellGroupClicked(this);
        }
    }

    @Override
    public void startRender() {
        L.i(TAG, "startRender");

        mLocalVideoCell.onResume();

        for (VideoCell cell : mRemoteVideoCells) {
            cell.onResume();
        }

        requestRender(true);
    }

    @Override
    public void pauseRender() {
        L.i(TAG, "pauseRender");

        mLocalVideoCell.onPause();

        for (VideoCell cell : mRemoteVideoCells) {
            cell.onPause();
        }

        requestRender(false);
    }

    @Override
    public void destroy() {
        if (mRemoteVideoCells != null) {
            mRemoteVideoCells.clear();
        }
        mRemoteVideoCells.clear();
        requestRender(false);
    }

    @Override
    public void setLocalVideoInfo(VideoInfo localViewInfo) {
        mLocalVideoInfo = localViewInfo;
        if (mLocalVideoCell != null) {
            mLocalVideoCell.setLayoutInfo(mLocalVideoInfo);
        }
    }

    @Override
    public void setFrameRate(int frameRate) {
        if (frameRate > 0) {
            this.frameRate = frameRate;
        }
    }

    @Override
    public void setChairmanUri(String chairmanUri) {
        mChairmanUri = chairmanUri;
    }

    @Override
    public void setCurrentIndex(int index) {
        L.i(TAG, "setCurrentIndex : " + index);
        mCurrentIndex = index;
    }

    @Override
    public synchronized void addOtherVideoInfos(List<VideoInfo> infos) {
        if (mRemoteVideoInfos != null && mRemoteVideoInfos.size() > 0) {
            List<VideoInfo> tmp = new ArrayList<>();
            for (VideoInfo oldInfo : mRemoteVideoInfos) {
                android.util.Log.d("debug", "oldInfo : " + oldInfo);
                for (VideoInfo newInfo : infos) {
                    if (!oldInfo.getRemoteID().equalsIgnoreCase(newInfo.getRemoteID())
                            && (mLocalVideoInfo != null && !newInfo.getRemoteID().equalsIgnoreCase(mLocalVideoInfo.getRemoteID()))) {
                        android.util.Log.d("debug", "newInfo : " + newInfo);
                        //infos.remove(newInfo);
                        tmp.add(newInfo);
                        //break;
                    }
                }
            }
            mRemoteVideoInfos.addAll(tmp);
            setRemoteVideoInfos(mRemoteVideoInfos);
        } else {
            for (VideoInfo newInfo : infos) {
                if ((mLocalVideoInfo != null && newInfo.getRemoteID().equalsIgnoreCase(mLocalVideoInfo.getRemoteID()))) {
                    infos.remove(newInfo);
                    break;
                }
            }
            setRemoteVideoInfos(infos);
        }
    }

    @Override
    public synchronized void removeOneVideoCell(VideoInfo info, boolean uiShake) {
        if (mRemoteVideoCells != null) {
            for (VideoCell cell : mRemoteVideoCells) {
                if (info.getRemoteID().equalsIgnoreCase(cell.getLayoutInfo().getRemoteID())
                        && (mLocalVideoInfo != null && !info.getRemoteID().equalsIgnoreCase(mLocalVideoInfo.getRemoteID()))) {
                    if (uiShake) {
                        cell.shake();
                    } else {
                        removeView(cell);
                        mRemoteVideoCells.remove(cell);
                        removeLayoutInfo(info);
                        requestLayout();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 展示人脸框
     *
     * @param faceViews
     */
    @Override
    public void showFaceView(List<FaceView> faceViews) {
        L.i(TAG, "showFaceView:" + faceViews.size());
        L.i(TAG, "fullScreenCell:" + mFullScreenVideoCell);
        if (mFullScreenVideoCell != null) {
            if (faceViews.size() > 0) {
                if (mFullScreenVideoCell.isFaceViewShows()) {
                    mFullScreenVideoCell.updateFaceView(faceViews);
                } else {
                    mFullScreenVideoCell.showFaceView(faceViews);
                }
            } else {
                mFullScreenVideoCell.removeAllFaceView();
            }
        }
    }

    /**
     * 移除人脸信息
     */
    @Override
    public void removeAllFaceView() {
        if (mFullScreenVideoCell != null) {
            mFullScreenVideoCell.removeAllFaceView();
        }
    }

    /**
     * 屏幕方向改变
     *
     * @param isLandscape
     */
    protected void onOrientationChanged(boolean isLandscape) {

    }

    private void removeLayoutInfo(VideoInfo info) {
        if (mRemoteVideoInfos != null && info != null) {
            for (VideoInfo layoutInfo : mRemoteVideoInfos) {
                if (layoutInfo.getRemoteID().equalsIgnoreCase(info.getRemoteID())) {
                    mRemoteVideoInfos.remove(layoutInfo);
                    break;
                }
            }
        }
    }

    /**
     * 更换usb camera
     *
     * @param isUvc
     */
    @Override
    public void updateCamera(boolean isUvc) {
        if (mLocalVideoCell != null) {
            mLocalVideoCell.updateCamrea(isUvc);
        }
    }

    /**
     * 获取全屏
     *
     * @return
     */
    public VideoCell getFullScreenVideoCell() {
        return mFullScreenVideoCell;
    }

    /**
     * 本地视频单元格
     *
     * @return
     */
    public VideoCell getLocalVideoCell() {
        return mLocalVideoCell;
    }

    /**
     * 获取远端视频信息
     *
     * @return
     */
    public List<VideoInfo> getRemoteVideoInfos() {
        return mRemoteVideoInfos;
    }

    public void setOnVideoCellListener(OnVideoCellListener listener) {
        onVideoCellListener = listener;
    }

    @Override
    public void onLongPress(MotionEvent e, VideoCell cell) {
        if (onVideoCellListener != null) {
            onVideoCellListener.onLongPress(e, cell);
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, VideoCell cell) {
        if (onVideoCellListener != null) {
            return onVideoCellListener.onDoubleTap(e, cell);
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
        if (onVideoCellListener != null) {
            return onVideoCellListener.onSingleTapConfirmed(e, cell);
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell) {
        if (onVideoCellListener != null) {
            return onVideoCellListener.onScroll(e1, e2, distanceX, distanceY, cell);
        }
        return false;
    }

    @Override
    public void onShakeDone(VideoCell cell) {
        android.util.Log.d("debug", "onShakeDone : " + cell.getLayoutInfo());
        removeView(cell);
        mRemoteVideoCells.remove(cell);
        removeLayoutInfo(cell.getLayoutInfo());
        requestLayout();
        if (onVideoCellListener != null) {
            onVideoCellListener.onShakeDone(cell);
        }
    }

    @Override
    public void onCancelAddother(VideoCell cell) {
        if (onVideoCellListener != null) {
            onVideoCellListener.onCancelAddother(cell);
        }
    }
}
