package com.xylink.sdk.sample.face;

import android.log.L;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import com.ainemo.sdk.model.FaceInfo;

import java.util.List;

/**
 * 人脸信息缓存
 * @author zhangyazhou
 */
public class FaceInfoCache {

    private static final String TAG = "FaceInfoCache";

    private LongSparseArray<LongSparseArray<FaceInfo>> infoGroup;

    public FaceInfoCache() {
        infoGroup = new LongSparseArray<>();
    }

    public void putFaceInfo(long participantId, @NonNull FaceInfo faceInfo) {
        L.i(TAG, "put face info, participantId:" + participantId + ", faceId:" + faceInfo.getFaceId());
        LongSparseArray<FaceInfo> array = infoGroup.get(participantId);
        if (array != null) {
            array.put(faceInfo.getFaceId(), faceInfo);
        } else {
            array = new LongSparseArray<>();
            array.put(faceInfo.getFaceId(), faceInfo);
            infoGroup.put(participantId, array);
        }
    }

    public void putFaceInfoList(long participantId, List<FaceInfo> infoList) {
        for (FaceInfo faceInfo: infoList) {
            putFaceInfo(participantId, faceInfo);
        }
    }

    @Nullable
    public FaceInfo getFaceInfo(long participantId, long faceId) {
        return infoGroup.get(participantId) == null ? null : infoGroup.get(participantId).get(faceId);
    }

    public boolean isCacheFace(long participantId, long faceId) {
        return infoGroup.get(participantId) != null && infoGroup.get(participantId).get(faceId) != null;
    }

    public void clear() {
        infoGroup.clear();
    }
}
