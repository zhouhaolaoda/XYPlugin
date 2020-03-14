package com.xylink.sdk.sample.face;

import android.content.Context;
import android.log.L;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * 人脸信息容器
 *
 * @author zhangyazhou
 * @date 2018/7/17
 */
public class FaceGroupView extends RelativeLayout {

    private static final String TAG = "FaceGroupView";

    private LongSparseArray<FaceView> faceArray;

    public FaceGroupView(Context context) {
        super(context);
        init();
    }

    public FaceGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        faceArray = new LongSparseArray<>();
    }

    public void addFaceView(FaceView faceView) {
        if (faceView != null) {
            faceArray.put(faceView.getFaceId(), faceView);
            addView(faceView);
            faceView.setContentSize();
            faceView.measure(0, 0);

            faceView.layout(faceView.getStartX(), faceView.getStartY(), faceView.getStartX() + faceView.getViewWidth() + 20, faceView.getStartY() + faceView.getHeight());
        }
        L.i(TAG, "addFaceView \n face group width:" + getWidth());
        L.i(TAG, "face group height:" + getHeight());
        L.i(TAG, "startX:" + faceView.getStartX());
        L.i(TAG, "startY:" + faceView.getStartY());
        L.i(TAG, "endX:" + faceView.getEndX());
        L.i(TAG, "endY:" + faceView.getEndY());
        L.i(TAG, "width:" + faceView.getWidth());
        L.i(TAG, "height:" + faceView.getHeight());
    }

    public void addFaceViews(List<FaceView> faceViews) {
        for (int i = 0; i < faceViews.size(); i++) {
            if (!isHaveFaceView(faceViews.get(i).getFaceId())) {
                addFaceView(faceViews.get(i));
            }
        }
    }

    public boolean isHaveFaceView(long faceId) {
        return faceArray.get(faceId) != null;
    }

    public void clear() {
        faceArray.clear();
        removeAllViews();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        for (int i = 0; i < faceArray.size(); i++) {
            FaceView faceView = faceArray.valueAt(i);
            faceView.measure(0, 0);
            faceView.setContentSize();
            faceView.layout(faceView.getStartX(), faceView.getStartY(), faceView.getStartX() + faceView.getViewWidth() + 20, faceView.getStartY() + faceView.getHeight());

        }
    }
}
