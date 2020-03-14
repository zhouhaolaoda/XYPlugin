package com.xylink.sdk.sample.face;

import android.content.Context;
import android.log.L;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

import com.xylink.sdk.sample.utils.SizeConvert;

/**
 * 人脸信息View组件
 *
 * @author zhangyazhou
 */
public class FaceView extends RelativeLayout {


    private static final String TAG = "FaceView";

    private static final int NAME_HEIGHT_IN_DP_BIG = 78;
    private static final int NAME_HEIGHT_IN_DP_SAMLL = 58;
    private static final int NAME_FACE_DIVIDE_IN_DP = 10;

    private static final int DEFAULT_WIDTH_IN_DP = 200;
    private static final int DEFAULT_HEIGHT_IN_DP = 288;
    public static final int FACE_SIZE_BIG = 360;

    private long faceId;
    private long participantId;
    private View view;
    private TextView nameTv;
    private TextView positionTv;
    private ImageView faceIv;
    private ImageView header_img;
    private int nameHeightInPxBig;
    private int nameHeightInPxSmall;
    RelativeLayout header_layout;
    private int nameFaceDivide;
    private int nameWidht = 496;
    private int defaultWidth;
    private int defaultHeight;
    private int screenWidth;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private boolean scaleL;
    private int lastHeight;
    private int nameHeightInPx;


    public FaceView(Context context) {
        super(context);
        initView(context);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        view = View.inflate(context, R.layout.layout_face_view, this);
        nameTv = (TextView) view.findViewById(R.id.face_view_name_tv);
        positionTv = (TextView) view.findViewById(R.id.face_view_position_tv);
        header_img = (ImageView) view.findViewById(R.id.header_img);
        faceIv = (ImageView) view.findViewById(R.id.face_view_face_iv);
        nameHeightInPxBig = SizeConvert.dp2px(getContext(), NAME_HEIGHT_IN_DP_BIG);
        nameHeightInPxSmall = SizeConvert.dp2px(getContext(), NAME_HEIGHT_IN_DP_SAMLL);
        header_layout = (RelativeLayout) view.findViewById(R.id.header_layout);
        screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        defaultWidth = SizeConvert.dp2px(getContext(), DEFAULT_WIDTH_IN_DP);
        defaultHeight = SizeConvert.dp2px(getContext(), DEFAULT_HEIGHT_IN_DP);
        nameFaceDivide = SizeConvert.dp2px(getContext(), NAME_FACE_DIVIDE_IN_DP);
//        header_layout.setBackgroundColor(Color.GREEN);
//        faceIv.setBackgroundColor(Color.RED);

    }

    public void setFaceId(long faceId) {
        this.faceId = faceId;
        if(faceId == -1){
            header_layout.setVisibility(GONE);
        }else{
            header_layout.setVisibility(VISIBLE);
        }
    }

    public long getFaceId() {
        return faceId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setName(String name) {
        nameTv.setText(name);
    }

    public void setPosition(String position) {
        if(TextUtils.isEmpty(position))
        {
            positionTv.setVisibility(GONE);
            positionTv.setText(position);
        }else{
            positionTv.setVisibility(VISIBLE);
            positionTv.setText(position);
        }

    }

    public void setContentSize() {

        LayoutParams paramsh = new LayoutParams(nameWidht,nameHeightInPx);

        int headpf = endX - startX >= nameWidht ? (endX - startX - nameWidht) / 2 : 0;
        paramsh.setMargins(headpf, 0, 0, 0);
        header_layout.setLayoutParams(paramsh);

        LayoutParams params = new LayoutParams(endX - startX, (endY - startY) - nameHeightInPx - nameFaceDivide);

        int facepf = endX - startX < nameWidht ? (nameWidht - endX + startX ) / 2 : 0;
        params.setMargins(facepf,nameHeightInPx + nameFaceDivide,0,0);
        faceIv.setLayoutParams(params);

    }

    public void setLayoutPosition(boolean isLocalFace, int left, int top, int right, int bottom) {
        if (isLocalFace) {
            startX = screenWidth - right;
            endX = screenWidth - left;
        } else {
            startX = left;
            endX = right;
        }

        if (endX - startX > FACE_SIZE_BIG) {
            nameHeightInPx = nameHeightInPxBig;
            startY = top - nameHeightInPxBig - nameFaceDivide;
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.face_view_name_big));
            positionTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.face_view_position_big));
            header_img.setBackgroundResource(R.drawable.bg_face_view_name_big);
        } else {
            nameHeightInPx = nameHeightInPxSmall;
            startY = top - nameHeightInPxSmall - nameFaceDivide;
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.face_view_name_small));
            positionTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.face_view_position_small));
            header_img.setBackgroundResource(R.drawable.bg_face_view_name_small);
        }

        endY = bottom;

        L.i(TAG, "screenWidth:" + screenWidth);
        L.i(TAG, "nameHeightInPxBig:" + nameHeightInPxBig);
        L.i(TAG, "nameFaceDivide:" + nameFaceDivide);
        L.i(TAG, "width:" + getWidth());
        L.i(TAG, "height:" + getHeight());

        scaleL = getHeight() - lastHeight > 0;
        lastHeight = getHeight();
        view.setLeft(getStartX());
        view.setRight(getEndX());
        view.setTop(getStartY());
        view.setBottom(getEndY() );

    }

    private void showSmall() {

    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public int getStartX() {
        if(endX -startX >=nameWidht)
        {
            return startX;
        }else{
            return startX - (nameWidht - endX + startX )/2;
        }

    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }


    public int getViewWidth(){
        if(endX -startX >=nameWidht)
        {
            return endX -startX;
        }else{
            return nameWidht;
        }
    }
}
