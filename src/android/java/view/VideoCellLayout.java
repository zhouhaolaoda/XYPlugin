package com.xylink.sdk.sample.view;

import android.view.MotionEvent;
import android.view.View;

import com.ainemo.sdk.otf.VideoInfo;
import com.xylink.sdk.sample.face.FaceView;
import com.xylink.sdk.sample.share.whiteboard.view.WhiteBoardCell;

import java.util.List;

public interface VideoCellLayout {

    /**
     * 本地静音
     * @param mute
     */
    void setMuteLocalAudio(boolean mute);

    /**
     * 关闭视频
     * @param mute
     * @param reason
     */
    void setMuteLocalVideo(boolean mute, String reason);

    /**
     * 语音模式
     * @param flag
     */
    void setAudioOnlyMode(boolean flag, boolean isLocalVideoMute);

    /**
     * 是否横屏
     * @param landscape
     */
    void setLandscape(boolean landscape);

    /**
     * 设置本地视频信息
     * @param localViewInfo
     */
    void setLocalVideoInfo(VideoInfo localViewInfo);

    /**
     * 设置远端视频信息
     * @param infos
     */
    void setRemoteVideoInfos(List<VideoInfo> infos);

    /**
     * 设置帧率
     * @param frameRate
     */
    void setFrameRate(int frameRate);

    /**
     * 更新usb camera
     * @param isUvc
     */
    void updateCamera(boolean isUvc);

    /**
     * 开始渲染
     */
    void startRender();

    /**
     * 暂停渲染
     */
    void pauseRender();

    /**
     * 销毁资源
     */
    void destroy();

    /**
     * 设置主席标记
     * @param uri
     */
    void setChairmanUri(String uri);

    /**
     * 视图索引
     * @param index
     */
    void setCurrentIndex(int index);

    /**
     * 添加邀请用户信息
     * @param infos
     */
    void addOtherVideoInfos(List<VideoInfo> infos);

    /**
     * 移除用户信息
     * @param info
     */
    void removeOneVideoCell(VideoInfo info, boolean uiShake);

    /**
     * 显示人脸信息
     * @param faceViews
     */
    void showFaceView(List<FaceView> faceViews);

    /**
     * 移除人脸信息
     */
    void removeAllFaceView();

    /**
     * 视频单元格监听器
     */
    interface OnVideoCellListener {

        /**
         * 长按视频单元格
         * @param e
         * @param cell
         */
        void onLongPress(MotionEvent e, VideoCell cell);

        /**
         * 双击视频单元格
         * @param e
         * @param cell
         * @return
         */
        boolean onDoubleTap(MotionEvent e, VideoCell cell);

        /**
         * 单击视频
         * @param e
         * @param cell
         * @return
         */
        boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell);

        /**
         * 视频单元滚动
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @param cell
         * @return
         */
        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell);

        /**
         * 长按白板
         * @param e
         * @param cell
         */
        void onLongPress(MotionEvent e, WhiteBoardCell cell);

        /**
         * 双击白板
         * @param e
         * @param cell
         * @return
         */
        boolean onDoubleTap(MotionEvent e, WhiteBoardCell cell);

        /**
         * 单击白板
         * @param e
         * @param cell
         * @return
         */
        boolean onSingleTapConfirmed(MotionEvent e, WhiteBoardCell cell);

        /**
         * 白板滚动
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @param cell
         * @return
         */
        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, WhiteBoardCell cell);

        /**
         * 结束邀请好友摇摆结束
         * @param cell
         */
        void onShakeDone(VideoCell cell);

        /**
         * 关闭邀请
         * @param cell
         */
        void onCancelAddother(VideoCell cell);

        /**
         * 全屏改变
         * @param cell
         */
        void onFullScreenChanged(VideoCell cell);

        /**
         * 白板消息发送
         * @param text
         */
        void onWhiteboardMessageSend(String text);

        /**
         * 单击视频跟布局
         * @param group
         */
        void onVideoCellGroupClicked(View group);
    }

    /**
     * 简单实现，避免子类需要实现所有接口
     */
    class SimpleVideoCellListener implements OnVideoCellListener {

        @Override
        public void onLongPress(MotionEvent e, VideoCell cell) {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e, VideoCell cell) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, VideoCell cell) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e, WhiteBoardCell cell) {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e, WhiteBoardCell cell) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, WhiteBoardCell cell) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, WhiteBoardCell cell) {
            return false;
        }

        @Override
        public void onShakeDone(VideoCell cell) {

        }

        @Override
        public void onCancelAddother(VideoCell cell) {

        }

        @Override
        public void onFullScreenChanged(VideoCell cell) {

        }

        @Override
        public void onWhiteboardMessageSend(String text) {

        }

        @Override
        public void onVideoCellGroupClicked(View group) {

        }
    }
}
