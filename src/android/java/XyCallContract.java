package com.xylink.sdk.sample;


import android.app.Activity;

import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.otf.NemoSDKListener;
import com.ainemo.sdk.otf.VideoInfo;
import com.xylink.sdk.sample.face.FaceView;

import java.util.List;

public interface XyCallContract {
    interface View extends BaseView<Presenter> {
        /**
         * 去电
         *
         * @param outgoingNumber
         */
        void showCallOutGoing(String outgoingNumber);

        /**
         * 来电
         *
         * @param callIndex
         * @param callNumber
         * @param callName
         */
        void showCallIncoming(int callIndex, String callNumber, String callName);

        /**
         * 挂断
         *
         * @param reason
         */
        void showCallDisconnected(String reason);

        /**
         * 接听
         */
        void showCallConnected();

        /**
         * 通话信息改变
         *
         * @param videoInfos      当前通话信息
         * @param hasVideoContent 是否有content共享
         */
        void showVideoDataSourceChange(List<VideoInfo> videoInfos, boolean hasVideoContent);

        /**
         * 会控消息回调
         *
         * @param operation        'mute':静音 'unmute':取消静音
         * @param isMuteIsDisabled 是否为强制 true强制(强制静音只能通过举手, 主持人同意才可发言), false非强制
         */
        void showConfMgmtStateChanged(String operation, boolean isMuteIsDisabled);

        /**
         * 下线
         *
         * @param code   状态码
         * @param reason 下线原因
         */
        void showKickout(int code, String reason);

        /**
         * 网络状况
         *
         * @param level 1、2、3、4个等级,差-中-良-优
         */
        void showNetLevel(int level);

        /**
         * 视频状态变化提示
         *
         * @param videoStatus 0：正常 1：本地网络不稳定 2：系统忙，视频质量降低 3：对方网络不稳定 4：网络不稳定，请稍候
         *                    5：WiFi信号不稳定
         */
        void showVideoStatusChange(int videoStatus);

        void showIMNotification(String values);

        void showAiFace(AIParam aiParam, boolean isLocalFace);

        /**
         * 录制
         *
         * @param isStart     true: 开始   false: 停止
         * @param displayName 录制名称
         * @param canStop     是否可以停止, 别人发起的录制不可停止
         */
        void showRecordStatusNotification(boolean isStart, String displayName, boolean canStop);

        /**
         * 共享图片
         *
         * @param state 状态
         */
        void updateSharePictures(NemoSDKListener.NemoDualState state); // 更新分享图片UI

        /**
         * 共享屏幕
         *
         * @param show 当前有人正在共享
         */
        void updateShareScreen(NemoSDKListener.NemoDualState show);

        /**
         * 通话邀请
         *
         * @param callIndex
         * @param callNumber
         * @param callName
         */
        void showInviteCall(int callIndex, String callNumber, String callName);

        void hideInviteCall();

        /**
         * 显示人脸信息
         *
         * @param faceViews 要显示的View
         */
        void showFaceView(List<FaceView> faceViews);

        /**
         * 获取Activity的Context
         *
         * @return Activity本身, 用于缓存FaceView时使用
         */
        Activity getCallActivity();

        /**
         * 获取主Cell的尺寸
         *
         * @return 尺寸结果, [0]=width, [1]=height
         */
        int[] getMainCellSize();

    }

    interface Presenter extends BasePresenter {
        /**
         * 处理Ai回调数据
         *
         * @param aiParam        ai数据
         * @param isMainCellInfo 是否为主屏数据信息
         */
        void dealAiParam(AIParam aiParam, boolean isMainCellInfo);

        void dealLocalAiParam(AIParam aiParam, boolean isMainCell);
    }
}
