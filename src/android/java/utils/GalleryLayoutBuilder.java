package com.xylink.sdk.sample.utils;

import android.log.L;
import android.text.TextUtils;

import com.ainemo.sdk.otf.LayoutElement;
import com.ainemo.sdk.otf.LayoutPolicy;
import com.ainemo.sdk.otf.LayoutPolicy.LayoutBuilder;
import com.ainemo.sdk.otf.ResolutionRatio;

import java.util.ArrayList;
import java.util.List;

import vulture.module.call.sdk.CallSdkJniListener.MiniRosterInfo;
import vulture.module.call.sdk.CallSdkJniListener.PostRosterInfo;

/**
 * 画廊模式(多个相同尺寸的屏的排列组合，如，吅，品，田等)
 */
public class GalleryLayoutBuilder implements LayoutBuilder {

    private static final String TAG = "GalleryLayoutBuilder";

    private LayoutPolicy layoutPolicy;

    @Override
    public List<LayoutElement> compute(LayoutPolicy policy) {
        layoutPolicy = policy;

        List<LayoutElement> layoutElements = new ArrayList<>();

        PostRosterInfo rosterInfo = layoutPolicy.getRosterInfo();

        if (rosterInfo != null) {
            List<MiniRosterInfo> rosterElements = rosterInfo.getPeopleRosterElements();
            if (rosterElements != null && rosterElements.size() > 0) {
                L.i(TAG, "rosterElements.size : " + rosterElements.size());

                int contentPid = rosterInfo.getContentSenderPid();
                int activeSpeakerPid = rosterInfo.getActiveSpeakerPid();

                for (int i = 0; i < rosterElements.size(); i++) {

                    MiniRosterInfo ros = rosterElements.get(i);
                    String deviceId = TextUtils.isEmpty(ros.getDeviceId()) ? "" : ros.getDeviceId();

                    LayoutElement layoutElement = new LayoutElement();
                    layoutElement.setParticipantId(ros.getParticipantId());

                    if (contentPid == ros.getParticipantId() || rosterElements.size() == 1) {
                        //内容720p/30fps
                        layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_HIGH);
                        layoutElements.add(0, layoutElement);
                    } else if (layoutPolicy.getConfMgmtInfo() != null && deviceId.equals(layoutPolicy.getConfMgmtInfo().chairManUri)
                            || layoutPolicy.getConfMgmtInfo() != null && deviceId.equals(layoutPolicy.getConfMgmtInfo().venueUri)
                            || activeSpeakerPid == ros.getParticipantId()) {
                        //主会场或主会场巡查其他参会者或者active speaker 720p/7.5fps
                        layoutElement.setResolutionRatio(ResolutionRatio.RESO_720P_BASE);
                        layoutElements.add(0, layoutElement);
                    } else if (rosterElements.size() == 2 || rosterElements.size() == 3) {
                        //品|田布局均为360p/15fps
                        layoutElement.setResolutionRatio(ResolutionRatio.RESO_360P_NORMAL);
                        layoutElements.add(layoutElement);
                    } else {
                        //大于四路的视频布局分辨率为180p/7.5fps
                        layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_BASE);
                        layoutElements.add(layoutElement);
                    }

                    //目前UI支持最大路4路video，即田字模式
                    if (layoutElements.size() > 4) {
                        //layoutElements.subList(4, layoutElements.size()).clear();
                    }
                }

                L.i(TAG, "layoutElements.size : " + rosterElements.size());
            }
        }

        return layoutElements;
    }

}
