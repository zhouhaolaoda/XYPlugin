package com.xylink.sdk.sample;

import android.log.L;

import com.ainemo.sdk.model.AICaptionInfo;
import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.model.FaceInfo;
import com.ainemo.sdk.model.FacePosition;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.NemoSDKListener;
import com.ainemo.sdk.otf.Roster;
import com.ainemo.sdk.otf.RosterWrapper;
import com.ainemo.sdk.otf.VideoInfo;
import com.ainemo.util.JsonUtil;
import com.xylink.sdk.sample.face.FaceInfoCache;
import com.xylink.sdk.sample.face.FaceView;
import com.xylink.sdk.sample.face.FaceViewCache;
import com.xylink.sdk.sample.net.DefaultHttpObserver;
import com.xylink.sdk.sample.utils.CollectionUtils;
import com.xylink.sdk.sample.utils.SpeakerLayoutBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * 通话业务: 包括从响铃到挂断的业务
 * NemoSDKListener 底层通话业务的回调
 */
public class XyCallPresenter implements XyCallContract.Presenter {
    private static final String TAG = "XyCallPresenter";
    private XyCallContract.View mCallView;
    private static final int DUAL_TYPE_CONTENT = 0;
    private static final int DUAL_TYPE_PICTURE = 3;

    // 人脸识别
    private FaceInfoCache faceInfoCache;
    private FaceViewCache faceViewCache;

    public XyCallPresenter(XyCallContract.View callView) {
        this.mCallView = callView;
        callView.setPresenter(this);
        faceInfoCache = new FaceInfoCache();
        faceViewCache = new FaceViewCache();
    }

    @Override
    public void start() {
        // xy sdk call business
        NemoSDK.getInstance().setNemoSDKListener(new NemoSDKListener() {
            @Override
            public void onCallStateChange(CallState state, String reason) {
                L.i(TAG, "onCallStateChange: " + state);
                switch (state) {
                    case CONNECTING:
                        // call connecting: see XyCallActivity#showOutgoing()
                        break;
                    case DISCONNECTED:
                        Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> mCallView.showCallDisconnected(reason));
                        break;
                    case CONNECTED:
                        NemoSDK.getInstance().setLayoutBuilder(new SpeakerLayoutBuilder());
                        Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> mCallView.showCallConnected());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onVideoDataSourceChange(List<VideoInfo> videoInfos, boolean hasVideoContent) {
                L.i(TAG, "onVideoDataSourceChange hasContent: " + hasVideoContent);
                Observable.just(videoInfos).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<VideoInfo>>() {
                    @Override
                    public void accept(List<VideoInfo> videoInfos) throws Exception {
                        mCallView.showVideoDataSourceChange(videoInfos, hasVideoContent);
                    }
                });
            }

            @Override
            public void onRosterChange(RosterWrapper roster) {
                if (roster != null) {
                    L.i(TAG, "onRosterChange called. roster.size=" + roster.getRosters().size());
                    for (Roster r : roster.getRosters()) {
                        L.i(TAG, "onVideoDataSourceChange is onRosterChange deviceName=" + r.getDeviceName() + ", pid=" + r.getParticipantId());
                    }
                }
            }

            @Override
            public void onConfMgmtStateChanged(int callIndex, String operation, boolean isMuteIsDisabled) {
                L.i(TAG, "onConfMgmtStateChanged: " + operation + " isMuteIsDisabled: " + isMuteIsDisabled);
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showConfMgmtStateChanged(operation, isMuteIsDisabled));
            }

            @Override
            public void onRecordStatusNotification(int callIndex, boolean isStart, String displayName) {
                L.i(TAG, "onRecordStatusNotification called");
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showRecordStatusNotification(isStart, displayName, false));
            }

            @Override
            public void onKickOut(int code, int reason) {
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showKickout(code, reason + ""));
            }

            @Override
            public void onNetworkIndicatorLevel(int level) {
                L.i(TAG, "onNetworkIndicatorLevel called. level=" + level);
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showNetLevel(level));
            }

            @Override
            public void onVideoStatusChange(int videoStatus) {
                L.i(TAG, "onVideoStatusChange called. videoStatus=" + videoStatus);
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showVideoStatusChange(videoStatus));
            }

            @Override
            public void onIMNotification(int callIndex, String type, String values) {
                L.i(TAG, "onIMNotification called. type==" + type + "==values=" + values);
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showIMNotification(values));
            }

            @Override
            public void onCallReceive(String name, String number, int callIndex) {
                L.i(TAG, "CallInfo nemoSDKDidReceiveCall callActivity is" + name + "==number==" + number + "==callIndex==" + callIndex);
            }

            @Override
            public void onDualStreamStateChange(NemoDualState state, String reason, int type) {
                L.i(TAG, "wang state: " + state + " type: " + type);
                Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(
                        integer -> {
                            if (type == DUAL_TYPE_PICTURE) {
                                mCallView.updateSharePictures(state);
                            } else if (type == DUAL_TYPE_CONTENT) {
                                mCallView.updateShareScreen(state);
                            }
                        }, throwable -> L.e(TAG, "dual stream got an error: " + throwable.getMessage()));
            }

            @Override
            public void onAiFace(AIParam aiParam, boolean isLocalFace) {
                Observable.just(0).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> mCallView.showAiFace(aiParam, isLocalFace));
            }

            @Override
            public void onAiCaption(AICaptionInfo aiCaptionInfo) {

            }

            // Note: 通话中接到来电只有两个状态, CONNECTING 响铃, DISCONNECTED 对方取消
            @Override
            public void onCallInvite(CallState state, int callIndex, String callNumber, String callName) {
                L.i(TAG, "onCallInvite: " + state + " number: " + callNumber);
                Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        switch (state) {
                            case CONNECTING:
                                mCallView.showInviteCall(callIndex, callNumber, callName);
                                break;
                            case DISCONNECTED:
                                mCallView.hideInviteCall();
                                break;
                        }
                    }
                });
            }
        });
    }

    //==============================================================================================
    //人脸识别业务
    //==============================================================================================
    @Override
    public void dealAiParam(AIParam aiParam, boolean isMainCellInfo) {
        L.i(TAG, "dealAiParam: " + isMainCellInfo);
        if (isMainCellInfo) {
            checkFaceInfoCache(aiParam);
            checkFaceViewCache(false, aiParam);
            showFaceView(aiParam);
        }
    }

    @Override
    public void dealLocalAiParam(AIParam aiParam, boolean isMainCell) {
        L.i(TAG, "dealLocalAiParam: " + isMainCell);
        if (isMainCell) {
            checkFaceInfoCache(aiParam);
            checkFaceViewCache(true, aiParam);
            showFaceView(aiParam);
        }
    }

    private void checkFaceInfoCache(AIParam aiParam) {
        L.i(TAG, "checkFaceInfoCache");
        List<FacePosition> noCacheList = new ArrayList<>();
        for (int i = 0; i < aiParam.getPositionVec().size(); i++) {
            FacePosition position = aiParam.getPositionVec().get(i);
            if (position.getFaceId() > 0) {
                if (!faceInfoCache.isCacheFace(aiParam.getParticipantId(), position.getFaceId())) {
                    noCacheList.add(position);
                }
            } else {
                FaceInfo faceInfo = new FaceInfo();
                faceInfo.setPosition("");
                faceInfo.setName("");
                faceInfo.setFaceId(position.getFaceId());
                faceInfoCache.putFaceInfo(aiParam.getParticipantId(), faceInfo);
                L.w(TAG, "face id 无效!");
            }
        }
        if (CollectionUtils.isNotEmpty(noCacheList)) {
            getFaceInfoFromServer(aiParam.getParticipantId(), noCacheList);
        }
    }

    private void checkFaceViewCache(boolean isLocalFace, AIParam aiParam) {
        L.i(TAG, "checkFaceViewCache, isLocalFace:" + isLocalFace + ", aiParam:" + aiParam);
        for (int i = 0; i < aiParam.getPositionVec().size(); i++) {
            FacePosition position = aiParam.getPositionVec().get(i);
            FaceView faceView = faceViewCache.getFaceInfoView(aiParam.getParticipantId(), position.getFaceId());
            if (faceView == null) {
                L.i(TAG, "get face info, faceId:" + position.getFaceId() + ", cellId:" + aiParam.getParticipantId());
                FaceInfo faceInfo = faceInfoCache.getFaceInfo(aiParam.getParticipantId(), position.getFaceId());
                if (faceInfo != null) {
                    faceView = new FaceView(mCallView.getCallActivity());
                    faceView.setPosition(faceInfo.getPosition());
                    faceView.setName(faceInfo.getName());
                    faceView.setFaceId(faceInfo.getFaceId());
                    faceView.setParticipantId(aiParam.getParticipantId());
                    faceInfoCache.putFaceInfo(aiParam.getParticipantId(), faceInfo);
                    faceViewCache.putFaceInfoView(aiParam.getParticipantId(), faceView);
                    calculatePosition(isLocalFace, faceView, position);
                } else {
                    L.w(TAG, " face info is null!!!");
                }
            } else {
                calculatePosition(isLocalFace, faceView, position);
            }
        }
    }

    private void showFaceView(AIParam aiParam) {
        L.i(TAG, "showFaceView");
        List<FaceView> showViews = new ArrayList<>();
        for (FacePosition position : aiParam.getPositionVec()) {
            FaceView faceView = faceViewCache.getFaceInfoView(aiParam.getParticipantId(), position.getFaceId());
            if (faceView != null) {
                showViews.add(faceView);
            }
        }
        mCallView.showFaceView(showViews);
    }

    private void calculatePosition(boolean isLocalFace, FaceView faceView, FacePosition position) {
        int[] cellSize = mCallView.getMainCellSize();
        float left = cellSize[0] * position.getLeft() / 10000.0F;
        float top = cellSize[1] * position.getTop() / 10000.0F;
        float right = cellSize[0] * position.getRight() / 10000.0F;
        float bottom = cellSize[1] * position.getBottom() / 10000.0F;
        L.i(TAG, "计算后的位置,left:" + left + ",top:" + top + ", right:" + right + ",bottom:" + bottom);
        faceView.setLayoutPosition(isLocalFace, ((int) left), ((int) top), ((int) right), ((int) bottom));
    }

    private void getFaceInfoFromServer(long participantId, List<FacePosition> positionList) {
        L.i(TAG, "getFaceInfoFromServer");
        if (CollectionUtils.isEmpty(positionList)) {
            L.w(TAG, "人脸位置信息为null!!!");
            return;
        }
        long[] faceIds = new long[positionList.size()];
        for (int i = 0; i < positionList.size(); i++) {
            faceIds[i] = positionList.get(i).getFaceId();
        }
        getMultiFaceInfo(participantId, faceIds);
    }

    private void getMultiFaceInfo(final long participantId, final long[] faceIds) {
        L.i(TAG, "getMultiFaceInfo:" + participantId + ",faceIds:" + (faceIds));
        NemoSDK.getInstance().getMultiFaceInfo(faceIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultHttpObserver<List<FaceInfo>>("getMultiFaceInfo") {
                    @Override
                    public void onNext(List<FaceInfo> list, boolean isJSON) {
                        L.i(TAG, "resp-facelist:" + JsonUtil.toJson(list));
                        faceInfoCache.putFaceInfoList(participantId, list);
                    }

                    @Override
                    public void onHttpError(HttpException exception, String errorData, boolean isJSON) {
                        super.onHttpError(exception, errorData, isJSON);
                        L.i(TAG, exception.message());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        super.onException(throwable);
                        L.i(TAG, throwable.getCause());
                    }
                });
    }
}
