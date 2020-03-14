package com.xylink.sdk.sample.utils;

import android.log.L;
import android.text.TextUtils;

import com.ainemo.sdk.otf.LayoutElement;
import com.ainemo.sdk.otf.LayoutPolicy;
import com.ainemo.sdk.otf.LayoutPolicy.LayoutBuilder;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.ResolutionRatio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vulture.module.call.sdk.CallSdkJniListener.MiniRosterInfo;
import vulture.module.call.sdk.CallSdkJniListener.PostRosterInfo;

/**
 * 演讲者模式(一个大屏，多个小屏)
 */
public class SpeakerLayoutBuilder implements LayoutBuilder {

    private static final String TAG = "SpeakerLayoutBuilder";

    private LayoutPolicy layoutPolicy;

    @Override
    public List<LayoutElement> compute(LayoutPolicy policy) {
        layoutPolicy = policy;

        List<LayoutElement> layoutElements = new ArrayList<>();

        PostRosterInfo rosterInfo = layoutPolicy.getRosterInfo();

        if (rosterInfo != null) {

            int contentSenderPid = rosterInfo.getContentSenderPid();
            int activeSpeakerPid = rosterInfo.getActiveSpeakerPid();

            List<MiniRosterInfo> peopleRosterElements = rosterInfo.getPeopleRosterElements();
            ArrayList<MiniRosterInfo> contentElements = rosterInfo.getContentRosterElements();

            List<MiniRosterInfo> rosters = new ArrayList<>();

            if (contentElements != null && contentElements.size() > 0 && contentSenderPid != 0) {
                MiniRosterInfo contentElement = selectOneContent(contentSenderPid, contentElements);
                if (contentElement != null) {
                    rosters.add(contentElement);
                }
            }
            if (peopleRosterElements != null && peopleRosterElements.size() > 0) {
                rosters.addAll(peopleRosterElements);
            }

            if (rosters != null && rosters.size() > 0) {
                L.i(TAG, "rosterElements.size : " + rosters.size());
                if (layoutPolicy.getConfMgmtInfo() != null && !TextUtils.isEmpty(layoutPolicy.getConfMgmtInfo().chairManUri)) {
                    //自己作为主会场不做特殊处理
                    if (layoutPolicy.getConfMgmtInfo().chairManUri.equals(String.valueOf(NemoSDK.getInstance().getUserId()).concat("@SOFT"))) {
                        unChairManMode(contentSenderPid, activeSpeakerPid, rosters, layoutElements);
                    } else if (chairManInRoster(contentSenderPid, rosters, layoutElements)) {
                        //do nothing
                    } else {
                        unChairManMode(contentSenderPid, activeSpeakerPid, rosters, layoutElements);
                    }
                } else {
                    unChairManMode(contentSenderPid, activeSpeakerPid, rosters, layoutElements);
                }

                if (layoutElements.size() > 5) {
                    layoutElements = layoutElements.subList(0, 5);
                }

                L.i(TAG, "layoutElements.size : " + layoutElements.size());
            }
        }

        return layoutElements;
    }

    private MiniRosterInfo selectOneContent(int contentPid, List<MiniRosterInfo> contentElements) {
        for (MiniRosterInfo element : contentElements) {
            if (contentPid == element.getParticipantId()) {
                return element;
            }
        }
        return null;
    }

    private boolean chairManInRoster(int contentPid, List<MiniRosterInfo> rosterElements, List<LayoutElement> layoutElements) {
        if (contentPid > 0) {
            for (int i = 0; i < rosterElements.size(); i++) {
                MiniRosterInfo ros = rosterElements.get(i);
                String deviceId = TextUtils.isEmpty(ros.getDeviceId()) ? "" : ros.getDeviceId();

                boolean contentThumbnail = layoutPolicy.getLockLayoutId() != ros.getParticipantId();

                if (contentPid == ros.getParticipantId()) {
                    LayoutElement layoutElement = new LayoutElement();
                    layoutElement.setParticipantId(ros.getParticipantId());
                    ResolutionRatio reso = contentThumbnail ? ResolutionRatio.RESO_720P_BASE : ResolutionRatio.RESO_720P_HIGH;
                    layoutElement.setResolutionRatio(reso);
                    layoutElements.add(0, layoutElement);
                } else if (deviceId.equals(layoutPolicy.getConfMgmtInfo().chairManUri)) {
                    LayoutElement layoutElement = new LayoutElement();
                    layoutElement.setParticipantId(ros.getParticipantId());
                    ResolutionRatio reso = contentThumbnail ? ResolutionRatio.RESO_720P_HIGH : ResolutionRatio.RESO_180P_NORMAL;
                    layoutElement.setResolutionRatio(reso);
                    layoutElements.add(layoutElement);
                }

                if (layoutElements.size() == 2) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < rosterElements.size(); i++) {
                MiniRosterInfo ros = rosterElements.get(i);
                String deviceId = TextUtils.isEmpty(ros.getDeviceId()) ? "" : ros.getDeviceId();

                //请求主会场的video layout
                if (deviceId.equals(layoutPolicy.getConfMgmtInfo().chairManUri)) {
                    LayoutElement layoutElement = new LayoutElement();
                    layoutElement.setParticipantId(ros.getParticipantId());
                    if (layoutPolicy.getLockLayoutId() == 0 || layoutPolicy.getLockLayoutId() == ros.getParticipantId()) {
                        layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                    } else {
                        layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_NORMAL);
                    }

                    layoutElements.add(0, layoutElement);

                    return true;
                }
            }
        }
        return false;
    }

    private void unChairManMode(int contentPid, int activeSpeakerPid, List<MiniRosterInfo> rosterElements, List<LayoutElement> layoutElements) {
        if (contentPid > 0) {
            contentLayout(contentPid, activeSpeakerPid, rosterElements, layoutElements);
        } else {
            unContentLayout(activeSpeakerPid, rosterElements, layoutElements);
        }
    }

    private void contentLayout(int contentPid, int activeSpeakerPid, List<MiniRosterInfo> rosterElements, List<LayoutElement> layoutElements) {
        for (int i = 0; i < rosterElements.size(); i++) {
            MiniRosterInfo ros = rosterElements.get(i);
            LayoutElement layoutElement = new LayoutElement();
            layoutElement.setParticipantId(ros.getParticipantId());

            boolean contentThumbnail = layoutPolicy.getLockLayoutId() != ros.getParticipantId();

            if (activeSpeakerPid > 0) {
                if (contentPid == ros.getParticipantId()) {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                    layoutElements.add(0, layoutElement);
                } else if (activeSpeakerPid == ros.getParticipantId()) {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_BASE);
                    layoutElements.add(layoutElement);
                }
            } else {
                if (contentPid == ros.getParticipantId()) {
                    //ResolutionRatio reso = contentThumbnail ? ResolutionRatio.RESO_720P_BASE : ResolutionRatio.RESO_720P_HIGH;
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                    layoutElements.add(0, layoutElement);
                } else {
                    //ResolutionRatio reso = contentThumbnail ? ResolutionRatio.RESO_720P_HIGH : ResolutionRatio.RESO_180P_NORMAL;
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_BASE);
                    layoutElements.add(layoutElement);
                }
            }
        }

        //内容模式只留两路流
        for (int i = layoutElements.size(); i > 2; i--) {
            layoutElements.remove(i - 1);
        }
    }

    private void unContentLayout(int activeSpeakerPid, List<MiniRosterInfo> rosterElements, List<LayoutElement> layoutElements) {
        if (layoutPolicy.getLockLayoutId() > 0) {
            lockLayout(activeSpeakerPid, rosterElements, layoutElements);
        } else {
            unLockLayout(activeSpeakerPid, rosterElements, layoutElements);
        }
    }

    /**
     * 锁定屏幕layout策略：
     * 如果local被锁定为主屏，则所有请求为：180p
     * 如果other被锁定为主屏，则请求720p,并且调换请求顺序,其余参会者为缩略图：180p
     *
     * @param activeSpeakerPid
     * @param rosterElements
     * @param layoutElements
     */
    private void lockLayout(int activeSpeakerPid, List<MiniRosterInfo> rosterElements, List<LayoutElement> layoutElements) {
        //如果local被锁定为主屏，则所有请求为：180p
        if (layoutPolicy.getLockLayoutId() == NemoSDK.getInstance().getUserId()) {
            for (int i = 0; i < rosterElements.size(); i++) {
                MiniRosterInfo ros = rosterElements.get(i);
                LayoutElement layoutElement = new LayoutElement();
                layoutElement.setParticipantId(ros.getParticipantId());
                layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_NORMAL);
                layoutElements.add(layoutElement);
            }
        } else {
            for (int i = 0; i < rosterElements.size(); i++) {
                MiniRosterInfo ros = rosterElements.get(i);
                LayoutElement layoutElement = new LayoutElement();
                layoutElement.setParticipantId(ros.getParticipantId());

                //如果other被锁定为主屏，则请求720p,并且调换请求顺序
                if (layoutPolicy.getLockLayoutId() == ros.getParticipantId()) {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                    layoutElements.add(layoutElement);
                    Collections.swap(layoutElements, 0, i);
                } else {//未锁定屏幕的为缩略图：180p
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_NORMAL);
                    layoutElements.add(layoutElement);
                }
            }
        }
    }

    /**
     * 未锁定屏幕layout策略：
     * 如果有音频输入，请求音频源为：720p, 否则为180p
     * 如果没有音频输入，请求一路720p,其余为180p
     *
     * @param activeSpeakerPid
     * @param rosterElements
     * @param layoutElements
     */
    private void unLockLayout(int activeSpeakerPid, List<MiniRosterInfo> rosterElements, List<LayoutElement> layoutElements) {
        for (int i = 0; i < rosterElements.size(); i++) {
            MiniRosterInfo ros = rosterElements.get(i);
            LayoutElement layoutElement = new LayoutElement();
            layoutElement.setParticipantId(ros.getParticipantId());
            if (activeSpeakerPid > 0) {
                if (activeSpeakerPid == ros.getParticipantId()) {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                    layoutElements.add(0, layoutElement);
                } else {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_NORMAL);
                    layoutElements.add(layoutElement);
                }
            } else {
                if (i == 0) {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                    layoutElements.add(0, layoutElement);
                } else {
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_NORMAL);
                    layoutElements.add(layoutElement);
                }
            }
        }
    }
}
