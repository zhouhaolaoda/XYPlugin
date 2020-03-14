/**
 * Copyright (C) 2019 XYLink Android SDK Source Project
 * <p>
 * Created by wanghui on 2019/1/22.
 */
package com.xylink.sdk.sample.view;

import android.content.Context;
import android.log.L;
import android.util.AttributeSet;

import com.ainemo.sdk.otf.VideoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 画廊模式视频布局视图类（多个相同尺寸的屏的排列组合，如，吅，品，田等）
 */
public class GalleryVideoView extends VideoCellGroup {

    private static final String TAG = "GalleryVideoView";


    public GalleryVideoView(Context context) {
        super(context);
    }

    public GalleryVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        L.i(TAG, "onLayout, left : " + l + ", top : " + t + ", right : " + r + ", bottom : " + b);

        layoutChilds(l, t, r, b);
    }

    /**
     * 设置远端视频信息
     * @param infos
     */
    @Override
    public synchronized void setRemoteVideoInfos(List<VideoInfo> infos) {
        if (infos != null) {
            mRemoteVideoInfos = infos;

            L.i(TAG, "setRemoteVideoInfos, mRemoteVideoInfos.size : " + mRemoteVideoInfos.size() + ", mThumbCells.size : " + mRemoteVideoCells.size());

            if (mRemoteVideoCells.size() > 0) {
                //1.compute none existed video cell
                List<VideoCell> toDel = new ArrayList<>();

                for (VideoCell cell : mRemoteVideoCells) {
                    for (int i = 0; i < mRemoteVideoInfos.size(); i++) {
                        VideoInfo info = mRemoteVideoInfos.get(i);
                        if (cell.getLayoutInfo().getParticipantId() == info.getParticipantId()) {
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
                    removeView(cell);
                    mRemoteVideoCells.remove(cell);
                }

                toDel.clear();
            } else {
                for (VideoCell cell : mRemoteVideoCells) {
                    removeView(cell);
                }
            }


            //3.update already exist cell & add new cell
            if (mRemoteVideoCells.size() > 0) {
                for (VideoInfo info : mRemoteVideoInfos) {
                    for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                        VideoCell cell = mRemoteVideoCells.get(i);

                        //update already exit cell
                        if (info.getParticipantId() == cell.getLayoutInfo().getParticipantId()) {
                            cell.setLayoutInfo(info);
                            break;
                        }

                        //add new cell
                        if (i == mRemoteVideoCells.size() - 1) {
                            mRemoteVideoCells.add(createRemoteCell(info, false));
                        }
                    }
                }
            } else {
                for (VideoInfo info : mRemoteVideoInfos) {
                    mRemoteVideoCells.add(createRemoteCell(info, false));
                }
            }

            L.i(TAG, "setRemoteVideoInfos,  mThumbCells.size : " + mRemoteVideoCells.size());

            //UI just support max four channel video, such as one local and three remote
            if (mRemoteVideoCells.size() > 3) {
                mRemoteVideoCells = mRemoteVideoCells.subList(0, 3);
            }

            //4.relayout accordingly child count
            requestLayout();
        } else {

            for (VideoCell cell : mRemoteVideoCells) {
                removeView(cell);
            }
            requestLayout();
        }
    }

    private void layoutChilds(int l, int t, int r, int b) {

        L.i(TAG, "layoutCells, childCount : " + getChildCount());

        switch (getChildCount()) {
            case 0:
                break;
            case 1:
                layoutOne(l, t, r, b);
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
            default:
                layoutFourCells(l, t, r, b);
                break;
        }
    }


    private void layoutOne(int l, int t, int r, int b) {
        mLocalVideoCell.setFullScreen(true);
        mLocalVideoCell.setRectVisible(false);
        mLocalVideoCell.setDraged(false);
        mLocalVideoCell.layout(l, t, r, b);
    }

    private void layoutCell(VideoCell cell, int l, int t, int r, int b) {
        cell.setFullScreen(false);
        cell.setRectVisible(true);
        cell.setDraged(false);
        cell.layout(l, t, r, b);
    }

    private void layoutTwoCells(int l, int t, int r, int b) {
        int cellWidth = ((r - l) - mCellPadding) / 2;
        int cellHeight = cellWidth * 9 / 16;

        L.i(TAG, "layoutTwoCells, cellWidth : " + cellWidth);

        //local video
        layoutCell(mLocalVideoCell, l, (b - t - cellHeight) / 2, cellWidth, (b - t - cellHeight) / 2 + cellHeight);
        if (mRemoteVideoCells.size() >= 1) {
            //one remote
            layoutCell(mRemoteVideoCells.get(0), cellWidth + mCellPadding, (b - t - cellHeight) / 2, r, (b - t - cellHeight) / 2 + cellHeight);
        }
    }

    private void layoutThreeCells(int l, int t, int r, int b) {
        int cellWidth = ((r - l) - mCellPadding) / 2;
        int cellHeight = ((b - t) - mCellPadding) / 2;

        L.i(TAG, "layoutThreeCells, cellWidth : " + cellWidth + ", cellHeight : " + cellHeight);

        //local video
        layoutCell(mLocalVideoCell, (r - l - cellWidth) / 2, t, (r - l - cellWidth) / 2 + cellWidth, cellHeight);
        if (mRemoteVideoCells.size() >= 2) {
            //one remote
            layoutCell(mRemoteVideoCells.get(0), l, cellHeight + mCellPadding, cellWidth, b);
            //two remote
            layoutCell(mRemoteVideoCells.get(1), cellWidth + mCellPadding, cellHeight + mCellPadding, r, b);
        }
    }

    private void layoutFourCells(int l, int t, int r, int b) {
        int cellWidth = ((r - l) - mCellPadding) / 2;
        int cellHeight = ((b - t) - mCellPadding) / 2;

        L.i(TAG, "layoutFourCells, cellWidth : " + cellWidth + ", cellHeight : " + cellHeight);

        //local video
        layoutCell(mLocalVideoCell, l, t, cellWidth, cellHeight);

        if (mRemoteVideoCells.size() >= 3) {
            //one remote
            layoutCell(mRemoteVideoCells.get(0), cellWidth + mCellPadding, t, r, cellHeight);
            //two remote
            layoutCell(mRemoteVideoCells.get(1), l, cellHeight + mCellPadding, cellWidth, b);
            //three remote
            layoutCell(mRemoteVideoCells.get(2), cellWidth + mCellPadding, cellHeight + mCellPadding, r, b);
        }
    }


    private VideoCell createRemoteCell(VideoInfo layoutInfo, boolean playCreateAnimation) {
        L.i(TAG, "createRemoteCell, remoteVidoeInfo " + layoutInfo);
        VideoCell cell = new VideoCell(playCreateAnimation, getContext(), this);
        cell.setLayoutInfo(layoutInfo);
        //cell.bringToFront();

        addView(cell);
        return cell;
    }

    @Override
    protected void createLocalCell(boolean isUvc) {
        mLocalVideoCell = new VideoCell(isUvc, false, getContext(), this);
        mLocalVideoCell.setId(VideoCell.LOCAL_VIEW_ID);
        //mLocalVideoCell.bringToFront();
        mLocalVideoCell.setLayoutInfo(mLocalVideoInfo);

        addView(mLocalVideoCell);
    }
}
