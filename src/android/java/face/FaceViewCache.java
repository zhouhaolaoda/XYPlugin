package com.xylink.sdk.sample.face;

import android.util.LongSparseArray;

import java.util.List;

/**
 * 人脸组件缓存器
 * @author zhangyazhou
 * @date 2018/7/17
 */
public class FaceViewCache {

    private LongSparseArray<LongSparseArray<FaceView>> viewArray;

    public FaceViewCache() {
        viewArray = new LongSparseArray<>();
    }

    public LongSparseArray<FaceView> getGroupView(long participantId) {
        return viewArray.get(participantId);
    }

    public void putFaceInfoView(long participantId, FaceView infoView) {
        LongSparseArray<FaceView> array = viewArray.get(participantId);
        if (array == null) {
            array = new LongSparseArray<>();
            array.put(infoView.getFaceId(), infoView);
            viewArray.put(participantId, array);
        } else {
            array.put(infoView.getFaceId(), infoView);
        }
    }

    public void putGroupFaceView(long participantId, List<FaceView> viewList) {
        LongSparseArray<FaceView> array = viewArray.get(participantId);
        if (array == null) {
            array = new LongSparseArray<>();
            viewArray.put(participantId, array);
        }
        for (FaceView faceView: viewList) {
            array.put(faceView.getFaceId(), faceView);
        }
    }

    public FaceView getFaceInfoView(long participantId, long faceId) {
        return viewArray.get(participantId) == null ? null : viewArray.get(participantId).get(faceId);
    }

    public boolean isCachedView(long participantId, long faceId) {
        return viewArray.get(participantId) != null && viewArray.get(participantId).get(faceId) != null;
    }

    public void clear() {
        viewArray.clear();
    }
}
