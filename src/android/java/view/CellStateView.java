package com.xylink.sdk.sample.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.log.L;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.ainemo.module.call.data.Enums;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

/**
 * Created by lsx on 28/08/2017.
 */
public class CellStateView extends ViewGroup {

    private static final String TAG = "CellStateView";

    protected Animation operatingAnim;
    protected OnCellStateEventListener mCellStateEventListener = null;
    private int ICON_SIZE_W = 15;
    private int ICON_SIZE_H = 18;
    private int ICON_SIZE_W_FS = 72;
    private int ICON_SIZE_H_FS = 58;
    private int ICON_SIZE_W_FS_AUDIO = 26;
    private int ICON_SIZE_H_FS_AUDIO = 26;
    private int ICON_SIZE_W_AUDIO = 16;
    private int ICON_SIZE_H_AUDIO = 16;
    private float TEXT_NAME_SIZE = 13;
    private float TEXT_NAME_SIZE_FS = 17;
    private String nameText;
    private String profilePictureUrl;
    private boolean videoMute = false;
    private boolean loadingState = false;
    private boolean noVideoState = false;
    private boolean audioOnlyState = false;
    private boolean addOtherState = false;
    private boolean audioMute = false;
    private boolean observerMode = false;
    private boolean cancelAddother = false;
    private boolean isUsingPSTN = false;
    private TextView mProfileName;
    private ImageView mProfileImage;
    private TextView mProfileLoadName;
    private TextView mVideoMuteText;
    private TextView mNoVideoText;
    private ImageView mAudioMuteImage;
    private ImageView mImageTurn;
    private ImageView mLoadingImage;
    private ImageView mPSTNImage;
    private TextView mPSTNText;
    private TextView mAudioOnlyText;
    private ImageView mBlack40Bg;
    private ImageView mCellStateBg;
    private ImageView mCellLoadingStateBg;
    private ImageView mMutedAudioBg;
    private ImageView mAddOtherBg;
    private ImageView mAddOtherFailedBg;
    private ImageView mCancelAddotherImage;
    private ImageView mAudioMuteImageSmall;
    private String mVideoLayouteState;
    private String mutereaseon;
    private int density;
    private int mScreenWidth;
    private CachedLayoutPosition params = null;
    private TextView fullDisplayName;
    private TextView smallDisplayName;
    private boolean isFullScreen;

    public CellStateView(Context context) {
        super(context);
        initView();
    }

    public CellStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CellStateView(Context context, OnCellStateEventListener cellStateEventListener) {
        super(context);
        setCellStateEventListener(cellStateEventListener);
        initView();
    }

    protected void initAnimation() {
        operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
    }

    public void setCellStateEventListener(OnCellStateEventListener cellStateEventListener) {
        this.mCellStateEventListener = cellStateEventListener;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        if (isFullScreen != fullScreen) {
            isFullScreen = fullScreen;
            requestLayout();
        }
    }

    private void initView() {
        initAnimation();

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        density = (int) displayMetrics.density;
        mScreenWidth = displayMetrics.widthPixels;

        ICON_SIZE_W = ICON_SIZE_W * density;
        ICON_SIZE_H = ICON_SIZE_H * density;
        ICON_SIZE_W_FS = ICON_SIZE_W_FS * density;
        ICON_SIZE_H_FS = ICON_SIZE_H_FS * density;

        ICON_SIZE_W_FS_AUDIO *= density;
        ICON_SIZE_H_FS_AUDIO *= density;

        ICON_SIZE_W_AUDIO *= density;
        ICON_SIZE_H_AUDIO *= density;

        View.inflate(getContext(), R.layout.cell_state_view, this);
        setClipChildren(false);


        mBlack40Bg = findViewById(R.id.cell_state_black_40_bg);
        mCellStateBg = findViewById(R.id.cell_state_bg);
        mCellLoadingStateBg = findViewById(R.id.loading_cell_state_bg);
        mMutedAudioBg = findViewById(R.id.cell_state_mute_audio_bg);
        mAddOtherBg = findViewById(R.id.cell_state_add_other_bg);
        mAddOtherFailedBg = findViewById(R.id.cell_state_add_other_failed_bg);

        mImageTurn = findViewById(R.id.bg_turn);
        mProfileName = findViewById(R.id.cell_state_user_profile_name);
        mProfileLoadName = findViewById(R.id.cell_state_user_profile_loadingname);
        mProfileImage = findViewById(R.id.cell_state_user_profile_pic);

        mAudioMuteImage = findViewById(R.id.cell_state_audio);
        mCancelAddotherImage = findViewById(R.id.cell_cancel_addother);

        fullDisplayName = findViewById(R.id.full_display_name);
        smallDisplayName = findViewById(R.id.small_display_name);

        mVideoMuteText = findViewById(R.id.cell_video_mute_text);

        mNoVideoText = findViewById(R.id.cell_no_video_text);
        mNoVideoText.setText(getResources().getString(R.string.call_no_video));

        mPSTNText = findViewById(R.id.cell_state_pstn_incall);
        mPSTNImage = findViewById(R.id.cell_state_pstn);
        mAudioOnlyText = findViewById(R.id.cell_state_audio_only_incall);


        mAudioMuteImageSmall = findViewById(R.id.cell_state_audio_small);

        mCancelAddotherImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (mCellStateEventListener != null) {
                    mCellStateEventListener.onClickCancelAddother(CellStateView.this);
                }
            }
        });

        mLoadingImage = findViewById(R.id.cell_state_loading);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        L.i("CellStateView onConfigurationChanged mScreenWidth:" + mScreenWidth);
    }

    public void setProfileName(String name) {
        if (name == nameText || (nameText != null && nameText.equals(name))) {
            return;
        }
        this.nameText = name;
        mProfileName.setText(nameText);
        mProfileName.measure(0, 0);
        mProfileLoadName.setText(nameText);
        mProfileLoadName.measure(0, 0);

    }

    public void setProfilePicture(String picUrl) {
        if (picUrl == profilePictureUrl || (profilePictureUrl != null && profilePictureUrl.equals(picUrl))) {
            return;
        }
        this.profilePictureUrl = picUrl;
        layoutSelf();
    }

    public void setMuteAudio(boolean mute) {

        this.audioMute = mute;
        layoutSelf();

        mAudioMuteImage.setVisibility(audioMute && isFullScreen ? VISIBLE : GONE);
        mAudioMuteImageSmall.setVisibility(audioMute && !isFullScreen ? VISIBLE : GONE);

        if (audioMute == mute) {
            return;
        }

        if (observerMode || noVideoState || videoMute || audioOnlyState) return;

//        mMutedAudioBg.setVisibility(audioMute ? VISIBLE : GONE);

//        if (videoMute) {
//            mAudioMuteImage.setVisibility(GONE);
//            mMutedAudioBg.setVisibility(GONE);
//            mAudioMuteImageSmall.setVisibility(GONE);
//        }


    }

    public void setAudioOnly(boolean flag) {
        if (audioOnlyState == flag) {
            return;
        }
        this.audioOnlyState = flag;
        if (audioOnlyState && (observerMode)) {
            return;
        }

        mProfileName.setVisibility(audioOnlyState ? VISIBLE : GONE);
        mAudioOnlyText.setVisibility(audioOnlyState ? VISIBLE : GONE);
        mCellStateBg.setVisibility(audioOnlyState ? VISIBLE : GONE);

        if (audioOnlyState) {
            mAudioMuteImageSmall.setVisibility(GONE);
            mAudioMuteImage.setVisibility(GONE);
            mMutedAudioBg.setVisibility(GONE);
            mBlack40Bg.setVisibility(GONE);
            mProfileImage.setVisibility(GONE);
        } else {
            mAudioMuteImageSmall.setVisibility(audioMute && !isFullScreen ? VISIBLE : GONE);
            mCellStateBg.setVisibility(VISIBLE);
        }
        layoutSelf();
    }

    public void setMuteVideo(boolean mute, String muteReason) {
        if (observerMode || noVideoState || audioOnlyState) return;

        this.videoMute = mute;
        this.mutereaseon = muteReason;

        mCellStateBg.setVisibility(videoMute ? VISIBLE : GONE);
        mVideoMuteText.setVisibility(videoMute ? VISIBLE : GONE);
        mProfileName.setVisibility(videoMute ? VISIBLE : GONE);

//        if (videoMute) {
//            mAudioMuteImage.setVisibility(GONE);
//            mAudioMuteImageSmall.setVisibility(GONE);
//            mMutedAudioBg.setVisibility(GONE);
//        }

        mVideoMuteText.measure(0, 0);

        layoutSelf();
    }

    public void setLoading(boolean loading) {
        if (loadingState == loading) {
            return;
        }
        this.loadingState = loading;
        if (loading) {
            if (observerMode) return;
        }
        mCellLoadingStateBg.setVisibility(loadingState ? VISIBLE : GONE);
        mProfileName.setVisibility(loadingState ? VISIBLE : GONE);
        if (loadingState) {
            mProfileImage.setVisibility(GONE);
            mLoadingImage.setVisibility(VISIBLE);
            mProfileLoadName.setVisibility(VISIBLE);
            mLoadingImage.startAnimation(operatingAnim);
        } else {
            mLoadingImage.clearAnimation();
            mLoadingImage.setVisibility(GONE);
            mProfileLoadName.setVisibility(GONE);
        }
        if (loadingState) {
            mAudioMuteImage.setVisibility(GONE);
            mMutedAudioBg.setVisibility(GONE);
            if (videoMute) {
                mVideoMuteText.setVisibility(VISIBLE);
            } else {
                mVideoMuteText.setVisibility(GONE);
            }
        }
        layoutSelf();
    }

    public void setUsingPSTN(boolean usingPSTN) {
        if (isUsingPSTN == usingPSTN) return;

        this.isUsingPSTN = usingPSTN;

        if (isUsingPSTN && (observerMode || loadingState || audioOnlyState)) {
            return;
        }

        mProfileName.setVisibility(isUsingPSTN ? VISIBLE : GONE);
        mPSTNImage.setVisibility(isUsingPSTN ? VISIBLE : GONE);
        mPSTNText.setVisibility(isUsingPSTN ? VISIBLE : GONE);
        mBlack40Bg.setVisibility(isUsingPSTN ? VISIBLE : GONE);

        if (isUsingPSTN) {
            mCellStateBg.setVisibility(GONE);
            mAudioMuteImage.setVisibility(GONE);
            mMutedAudioBg.setVisibility(GONE);
            if (videoMute) {

                mVideoMuteText.setVisibility(VISIBLE);
            } else {
                mVideoMuteText.setVisibility(GONE);
            }
        }
        layoutSelf();
    }

    public void setNoVideo(String videostate, boolean noVideo, String reason) {
        L.i(TAG, "noVideo: " + noVideo + " reason: " + reason + " state: " + videostate);

        if (noVideoState == noVideo && mutereaseon == reason && videostate == mVideoLayouteState) {
            return;
        }
        this.noVideoState = noVideo;
        this.mutereaseon = reason;
        this.mVideoLayouteState = videostate;
        if (noVideo) {
            if (observerMode || loadingState) return;
        }

        mProfileName.setVisibility(noVideo ? VISIBLE : GONE);
        mNoVideoText.setVisibility(noVideo ? VISIBLE : GONE);
        mCellStateBg.setVisibility(noVideo ? VISIBLE : GONE);
        if (noVideo) {
            mAudioMuteImage.setVisibility(GONE);
            mMutedAudioBg.setVisibility(GONE);
            if (videoMute) {

                mVideoMuteText.setVisibility(VISIBLE);
            } else {
                mVideoMuteText.setVisibility(GONE);
            }
        }
        layoutSelf();

    }

    public void setCancleAddother(boolean cancle) {
        if (cancelAddother == cancle) {
            return;
        }

        mCancelAddotherImage.setVisibility(cancle ? VISIBLE : GONE);
        cancelAddother = cancle;
        layoutSelf();
    }

    public boolean getCancelAddother() {
        return cancelAddother;
    }

    public void setObserverMode(boolean ob) {
        if (observerMode == ob) {
            return;
        }
        if (ob) {
            mProfileName.setVisibility(VISIBLE);
            mLoadingImage.setVisibility(GONE);
            mProfileLoadName.setVisibility(GONE);
            mBlack40Bg.setVisibility(VISIBLE);
            mMutedAudioBg.setVisibility(GONE);
            mCellStateBg.setVisibility(GONE);
            mVideoMuteText.setVisibility(GONE);
            mNoVideoText.setVisibility(GONE);
            mAudioMuteImage.setVisibility(GONE);
            mProfileImage.setVisibility(VISIBLE);
        } else if (this.observerMode) {
            mBlack40Bg.setVisibility(GONE);
            mCellStateBg.setVisibility(GONE);
            mProfileName.setVisibility(GONE);
            mProfileName.setTextColor(getContext().getResources().getColor(android.R.color.white));
            mProfileImage.setVisibility(GONE);

        }
        this.observerMode = ob;
        layoutSelf();
    }

    @SuppressLint("WrongCall")
    protected void layoutSelf() {
        if (params != null) {
            onLayout(true, params.getL(), params.getT(), params.getR(), params.getB());
            invalidate();
        } else {
            requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (params == null) {
            params = new CachedLayoutPosition(l, t, r, b);
        } else {
            params.setVals(l, t, r, b);
        }
        int width = r - l;
        int height = b - t;
        int gap = density * 5;
        int fullgap = density * 10;
        int iconWidth, iconHeight;
        float nameTextSize;
        //isFullScreen = width > mScreenWidth / 5;//|| r > 350;  the r is not fit to 2K resolution screen e.g MX4 pro.
        L.i(TAG, "name : " + nameText + ", isFullScreen : " + isFullScreen + ", isUsingPSTN : " + isUsingPSTN + ", videoMute : " + videoMute + ", muteReason : " + mutereaseon
                + ", audioMute : " + audioMute + ", audioOnlyState : " + audioOnlyState + ", noVideoState : " + noVideoState + ", loadingState : " + loadingState + ", addOtherState : " + addOtherState
                + ", mVideoLayouteState : " + mVideoLayouteState + ", width : " + width + ", height : " + height + ", hashCode : " + this.hashCode());

        if (isFullScreen) {
            iconWidth = ICON_SIZE_W_FS;
            iconHeight = ICON_SIZE_H_FS;
            nameTextSize = TEXT_NAME_SIZE_FS;
            if (audioMute) {
                iconWidth = ICON_SIZE_W_FS_AUDIO;
                iconHeight = ICON_SIZE_H_FS_AUDIO;
            }
        } else {
            iconWidth = ICON_SIZE_W;
            iconHeight = ICON_SIZE_H;
            nameTextSize = TEXT_NAME_SIZE;
            if (audioMute) {
                iconWidth = ICON_SIZE_W_AUDIO;
                iconHeight = ICON_SIZE_H_AUDIO;
            }
        }

        int pnLeft = 0;
        int marginTop = 100 * density;
        if (isUsingPSTN) {
            //PSTN icon
            int maRight = gap + iconWidth;
            int bottom = gap + iconHeight;
            if (!isFullScreen) {
                mPSTNImage.setVisibility(GONE);
                mPSTNText.setVisibility(VISIBLE);
                mPSTNText.setText(getResources().getString(R.string.pstn_app_cell_text));
                mPSTNText.layout(width / 2 - mPSTNText.getMeasuredWidth() / 2, height / 2 - mPSTNText.getMeasuredHeight() / 2, width / 2 + mPSTNText.getMeasuredWidth(), height / 2 + mPSTNText.getMeasuredHeight());
            } else {
                mPSTNText.setVisibility(GONE);
                mPSTNImage.setVisibility(VISIBLE);
                mProfileImage.setVisibility(GONE);
                int left = (width - iconWidth) / 2;
                mPSTNImage.layout(left, marginTop + 10 * density, left + iconWidth, marginTop + iconHeight + 10 * density);
            }
            pnLeft = maRight + gap;
            // 语音通话模式静音图标状态判断
            if (audioMute) {
                //audio mute icon
                if (isFullScreen) {
                    mAudioMuteImage.setVisibility(VISIBLE);
                    mAudioMuteImageSmall.setVisibility(GONE);
                    mAudioMuteImage.layout(width - fullgap - iconWidth, fullgap, width - fullgap, iconHeight + fullgap);
                } else {
                    mAudioMuteImage.setVisibility(GONE);
                    mAudioMuteImageSmall.setVisibility(VISIBLE);
                    mAudioMuteImageSmall.layout(width - gap - iconWidth, gap, width - gap, gap + iconHeight);
                }
            }
        } else if (audioMute) {
            //audio mute icon
            if (isFullScreen) {
                mAudioMuteImage.setVisibility(VISIBLE);
                mAudioMuteImageSmall.setVisibility(GONE);
                mAudioMuteImage.layout(width - fullgap - iconWidth, fullgap, width - fullgap, iconHeight + fullgap);
            } else {
                mAudioMuteImage.setVisibility(GONE);
                mAudioMuteImageSmall.setVisibility(VISIBLE);
                mAudioMuteImageSmall.layout(width - gap - iconWidth, gap, width - gap, gap + iconHeight);
            }
        }
        //User profile name
        if (isFullScreen) {
            mProfileName.setTextSize(16);
            mProfileLoadName.setTextSize(16);
            mVideoMuteText.setTextSize(13);
            mNoVideoText.setTextSize(13);
            mPSTNText.setTextSize(13);
            mAudioOnlyText.setTextSize(13);
            fullDisplayName.setTextSize(11);
        } else {
            if (mProfileName.getTextSize() != nameTextSize) {
                mProfileName.setTextSize(nameTextSize);
                mProfileLoadName.setTextSize(nameTextSize);
            }

            if (mVideoMuteText.getTextSize() != nameTextSize) {
                mVideoMuteText.setTextSize(nameTextSize);
            }
            if (mNoVideoText.getTextSize() != nameTextSize) {
                mNoVideoText.setTextSize(nameTextSize);
            }
            mPSTNText.setTextSize(TEXT_NAME_SIZE);
            mAudioOnlyText.setTextSize(TEXT_NAME_SIZE);
            smallDisplayName.setTextSize(11);
        }
        mProfileName.setText(nameText);
        mProfileLoadName.setText(nameText);
        fullDisplayName.setText(nameText);
        mProfileName.measure(0, 0);
        mProfileLoadName.measure(0, 0);
        mVideoMuteText.measure(0, 0);
        mNoVideoText.measure(0, 0);
        mPSTNText.measure(0, 0);
        fullDisplayName.measure(0, 0);
        smallDisplayName.measure(0, 0);
        smallDisplayName.setMaxWidth(width - gap * 2);

        if (isFullScreen) {
            fullDisplayName.setVisibility(VISIBLE);
            fullDisplayName.layout(fullgap, fullgap, fullDisplayName.getMeasuredWidth() + fullgap, fullDisplayName.getMeasuredHeight() + fullgap);
            smallDisplayName.setVisibility(GONE);

            if (videoMute) {
                mVideoMuteText.measure(0, 0);
                mProfileName.layout((width - mProfileName.getMeasuredWidth()) / 2, (height - mProfileName.getMeasuredHeight()) / 2 + 30 * density, (width + mProfileName.getMeasuredWidth()) / 2, (height + mProfileName.getMeasuredHeight()) / 2 + 30 * density);
                switch (mutereaseon) {
                    case "MuteByUser":
                        if (isUsingPSTN) {
                            mPSTNImage.setVisibility(VISIBLE);
                            mProfileImage.setVisibility(GONE);
                            mVideoMuteText.setText(getResources().getString(R.string.cell_state_pstn_incall));
                            mVideoMuteText.setVisibility(VISIBLE);
                        } else if (audioOnlyState) {
                            mPSTNImage.setVisibility(VISIBLE);
                            mProfileImage.setVisibility(GONE);
                            mAudioOnlyText.setVisibility(GONE);
                            mPSTNText.setVisibility(GONE);
                            mVideoMuteText.setVisibility(VISIBLE);
                            mVideoMuteText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                        } else {
                            mPSTNImage.setVisibility(GONE);
                            mProfileImage.setVisibility(VISIBLE);
                            mVideoMuteText.setText(getResources().getString(R.string.MuteByUser));
                        }
                        break;
                    case "MuteByNoInput":
                        mVideoMuteText.setText(getResources().getString(R.string.MuteByNoInput));
                        break;
                    case "MuteByBWLimit":
                        mVideoMuteText.setText(getResources().getString(R.string.MuteByBWLimit));
                        break;
                    case "MuteByConfMgmt":
                        if (isUsingPSTN) {
                            mVideoMuteText.setText(getResources().getString(R.string.cell_state_pstn_incall));
                            mVideoMuteText.setVisibility(VISIBLE);
                        } else {
                            if (audioOnlyState) {
                                mVideoMuteText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                            } else {
                                mVideoMuteText.setText(getResources().getString(R.string.MuteByConfMgmt));
                            }
                        }
                        break;
                    case "MuteByMyself":
                        if (isUsingPSTN) {
                            mVideoMuteText.setText(getResources().getString(R.string.cell_state_pstn_incall));
                            mVideoMuteText.setVisibility(VISIBLE);
                            mAudioOnlyText.setVisibility(GONE);
                        } else if (audioOnlyState) {
                            mVideoMuteText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                            mVideoMuteText.setVisibility(VISIBLE);
                            mAudioOnlyText.setVisibility(GONE);
                        } else {
                            mVideoMuteText.setVisibility(VISIBLE);
                            mVideoMuteText.setText(getResources().getString(R.string.call_video_mute));
                        }

                        break;
                }
                mCellStateBg.setVisibility(VISIBLE);
                mVideoMuteText.setGravity(Gravity.CENTER_HORIZONTAL);
                mVideoMuteText.setWidth(width);
                mVideoMuteText.measure(0,0);

                Log.d(TAG, "width : " + width + ", textWidth : " + mVideoMuteText.getMeasuredWidth() + ", name : " + mProfileName.getText() + ", hashCode : " + hashCode() + ", isFullCell : " + isFullScreen);
                mVideoMuteText.layout((width - mVideoMuteText.getMeasuredWidth()) / 2, (height - mVideoMuteText.getMeasuredHeight() + mProfileName.getMeasuredHeight() + 60 * density) / 2 + 10 * density, (width + mVideoMuteText.getMeasuredWidth()) / 2, (height + mVideoMuteText.getMeasuredHeight() + 60 * density + mProfileName.getMeasuredHeight()) / 2 + 10 * density);

            } else if (noVideoState) {
                mNoVideoText.measure(0, 0);
                mProfileName.layout((width - mProfileName.getMeasuredWidth()) / 2, (height - mProfileName.getMeasuredHeight()) / 2 + 30 * density, (width + mProfileName.getMeasuredWidth()) / 2, (height + mProfileName.getMeasuredHeight()) / 2 + 30 * density);
                if (Enums.LAYOUT_STATE_NO_BANDWIDTH.equals(mVideoLayouteState)) {
                    mNoVideoText.setText(getResources().getString(R.string.call_no_video));
                } else if (Enums.LAYOUT_STATE_NO_DECODER.equals(mVideoLayouteState)) {
                    mNoVideoText.setText(getResources().getString(R.string.call_video_mute));
                } else {
                    switch (mutereaseon) {
                        case "MuteByUser":
                            mNoVideoText.setText(getResources().getString(R.string.MuteByUser));
                            break;
                        case "MuteByNoInput":
                            mNoVideoText.setText(getResources().getString(R.string.MuteByNoInput));
                            break;
                        case "MuteByBWLimit":
                            mNoVideoText.setText(getResources().getString(R.string.MuteByBWLimit));
                            break;
                        case "MuteByConfMgmt":
                            if (isUsingPSTN) {
                                mVideoMuteText.setText(getResources().getString(R.string.cell_state_pstn_incall));
                            } else {
                                if (audioOnlyState) {
                                    mNoVideoText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                                } else {
                                    mNoVideoText.setText(getResources().getString(R.string.MuteByConfMgmt));
                                }
                            }
                            break;
                        case "MuteByMyself":
                            mVideoMuteText.setText(getResources().getString(R.string.call_video_mute));
                            break;
                    }
                }
                mNoVideoText.setVisibility(VISIBLE);
                mPSTNText.setVisibility(GONE);
                mNoVideoText.setGravity(Gravity.CENTER_HORIZONTAL);
                mNoVideoText.setWidth(width);
                mNoVideoText.measure(0,0);
                mNoVideoText.layout((width - mNoVideoText.getMeasuredWidth()) / 2, (height - mNoVideoText.getMeasuredHeight() + mProfileName.getMeasuredHeight() + 60 * density) / 2 + 10 * density, (width + mNoVideoText.getMeasuredWidth()) / 2, (height + mNoVideoText.getMeasuredHeight() + 60 * density + mProfileName.getMeasuredHeight()) / 2 + 10 * density);
            } else if (isUsingPSTN) {
                mNoVideoText.setVisibility(GONE);
                mPSTNText.setVisibility(VISIBLE);

                mProfileName.setVisibility(VISIBLE);
                mCellStateBg.setVisibility(VISIBLE);

                mPSTNText.setText(getResources().getString(R.string.cell_state_pstn_incall));
                mPSTNText.measure(0, 0);
                int twidth = mPSTNText.getMeasuredWidth();
                int left = (width - twidth) / 2;
                int top = marginTop + iconHeight + fullgap;

                mPSTNText.layout(left, (height - mPSTNText.getMeasuredHeight()) / 2 + 30 * density, left + twidth, (height + mPSTNText.getMeasuredHeight()) / 2 + 30 * density);
                mProfileName.layout((width - mProfileName.getMeasuredWidth()) / 2, top + 60 * density, (width + mProfileName.getMeasuredWidth()) / 2, (top + mProfileName.getMeasuredHeight()) + 60 * density);

            } else if (audioOnlyState) {
                mProfileName.layout((width - mProfileName.getMeasuredWidth()) / 2, (height - mProfileName.getMeasuredHeight()) / 2 + 30 * density, (width + mProfileName.getMeasuredWidth()) / 2, (height + mProfileName.getMeasuredHeight()) / 2 + 30 * density);
                mProfileName.setVisibility(VISIBLE);
                mProfileImage.setVisibility(VISIBLE);

                mCellStateBg.setVisibility(VISIBLE);
                mBlack40Bg.setVisibility(GONE);
                mVideoMuteText.setVisibility(GONE);

                mAudioOnlyText.measure(0, 0);
                mAudioOnlyText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                mAudioOnlyText.layout((width - mAudioOnlyText.getMeasuredWidth()) / 2, (height - mAudioOnlyText.getMeasuredHeight() + mProfileName.getMeasuredHeight() + 60 * density) / 2 + 10 * density, (width + mAudioOnlyText.getMeasuredWidth()) / 2, (height + mAudioOnlyText.getMeasuredHeight() + 60 * density + mProfileName.getMeasuredHeight()) / 2 + 10 * density);
            } else if (loadingState) {
                int pnHeight = mProfileName.getMeasuredHeight();

                int nameTextMarginTop = (40 * density - pnHeight) / 2 + fullgap;
                mProfileName.setGravity(Gravity.LEFT);
                mProfileName.layout(fullgap + (40 + 7) * density, nameTextMarginTop, r, nameTextMarginTop + pnHeight);
                mProfileName.setVisibility(VISIBLE);
                mProfileImage.setVisibility(GONE);
                mProfileLoadName.setVisibility(VISIBLE);
                mProfileLoadName.setGravity(Gravity.CENTER);

                int liLeft = width / 2 - mLoadingImage.getDrawable().getIntrinsicWidth() / 2;
                int liTop = height / 2 - mLoadingImage.getDrawable().getIntrinsicHeight() / 2 - 60;
                mLoadingImage.layout(liLeft, liTop, liLeft + mLoadingImage.getDrawable().getIntrinsicWidth(), liTop + mLoadingImage.getDrawable().getIntrinsicHeight());
                mProfileLoadName.layout(0, liTop + mLoadingImage.getDrawable().getIntrinsicHeight() + 30, r, liTop + mLoadingImage.getDrawable().getIntrinsicHeight() + 30 + nameTextMarginTop + pnHeight);

            } else {
                int pnHeight = mProfileName.getMeasuredHeight();
                mProfileName.setGravity(Gravity.LEFT);
                int nameTextMarginTop = iconHeight / 2 - (int) nameTextSize / 2;
                mProfileName.layout(pnLeft, nameTextMarginTop, r, gap + nameTextMarginTop + pnHeight);
            }
        } else {

            fullDisplayName.setVisibility(GONE);
            smallDisplayName.setVisibility(VISIBLE);
            smallDisplayName.setText(nameText);
            smallDisplayName.measure(0, 0);
            int smallDnameMeasuredHeight = smallDisplayName.getMeasuredHeight();
            int smallDnameMeasuredWidth = smallDisplayName.getMeasuredWidth();
            if (smallDnameMeasuredWidth > width) {
                smallDnameMeasuredWidth = width - gap * 2;
            }

            smallDisplayName.layout(gap, b - smallDnameMeasuredHeight - gap, smallDnameMeasuredWidth + gap, b - gap);
            mVideoMuteText.measure(0, 0);
            mNoVideoText.measure(0, 0);
            mAudioOnlyText.measure(0, 0);
            if (videoMute) {
                if (isUsingPSTN) {
                    mVideoMuteText.setVisibility(GONE);
                    mPSTNImage.setVisibility(GONE);
                    mPSTNText.setVisibility(VISIBLE);
                    mPSTNText.setText(getResources().getString(R.string.pstn_app_cell_text));
                    mPSTNText.measure(0, 0);
                    mPSTNText.layout(width / 2 - mPSTNText.getMeasuredWidth() / 2, height / 2 - mPSTNText.getMeasuredHeight() / 2, width / 2 + mPSTNText.getMeasuredWidth(), height / 2 + mPSTNText.getMeasuredHeight());
                } else if (audioOnlyState) {
                    mPSTNText.setVisibility(GONE);
                    mVideoMuteText.setVisibility(GONE);
                    mAudioOnlyText.setVisibility(VISIBLE);
                    mAudioOnlyText.setGravity(Gravity.CENTER_HORIZONTAL);
                    mAudioOnlyText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                    mAudioOnlyText.layout((width - mAudioOnlyText.getMeasuredWidth()) / 2, (height - mAudioOnlyText.getMeasuredHeight()) / 2, (width + mAudioOnlyText.getMeasuredWidth()) / 2, (height + mAudioOnlyText.getMeasuredHeight()) / 2);
                } else {
                    mPSTNText.setVisibility(GONE);
                    mVideoMuteText.setVisibility(VISIBLE);
                    mVideoMuteText.setText(getResources().getString(R.string.call_video_mute));// Note if no this line, ... will be shown instead. why?
                }

                Log.d(TAG, "width : " + width + ", textWidth : " + mVideoMuteText.getMeasuredWidth() + ", name : " + mProfileName.getText() + ", hashCode : " + hashCode() + ", isFullCell : " + isFullScreen);
                mVideoMuteText.setGravity(Gravity.CENTER_HORIZONTAL);
                mVideoMuteText.setWidth(width);
                mVideoMuteText.measure(0,0);
                if(mVideoMuteText.getMeasuredWidth() > width) {
                    mVideoMuteText.layout(l, (height - mVideoMuteText.getMeasuredHeight()) / 2, r, (height + mVideoMuteText.getMeasuredHeight()) / 2);
                } else {
                    mVideoMuteText.layout((width - mVideoMuteText.getMeasuredWidth()) / 2, (height - mVideoMuteText.getMeasuredHeight()) / 2, (width + mVideoMuteText.getMeasuredWidth()) / 2, (height + mVideoMuteText.getMeasuredHeight()) / 2);
                }

                mProfileName.layout((width - mProfileName.getMeasuredWidth()) / 2, (height - mProfileName.getMeasuredHeight()) / 2 + 30 * density, (width + mProfileName.getMeasuredWidth()) / 2, (height + mProfileName.getMeasuredHeight()) / 2 + 30 * density);
                mCellStateBg.setVisibility(VISIBLE);
            } else if (noVideoState) {
                mPSTNText.setVisibility(GONE);
                mNoVideoText.setVisibility(VISIBLE);
                mNoVideoText.setGravity(Gravity.CENTER_HORIZONTAL);
                mNoVideoText.setText(getResources().getString(R.string.call_video_mute));
                mNoVideoText.setWidth(width);
                mNoVideoText.measure(0,0);
                if(mVideoMuteText.getMeasuredWidth() > width) {
                    mNoVideoText.layout(l, (height - mNoVideoText.getMeasuredHeight()) / 2, r, (height + mNoVideoText.getMeasuredHeight()) / 2);
                } else {
                    mNoVideoText.layout((width - mNoVideoText.getMeasuredWidth()) / 2, (height - mNoVideoText.getMeasuredHeight()) / 2, (width + mNoVideoText.getMeasuredWidth()) / 2, (height + mNoVideoText.getMeasuredHeight()) / 2);
                }
                mCellStateBg.setVisibility(VISIBLE);
            } else if (isUsingPSTN) {
                mNoVideoText.setVisibility(GONE);
                mCellStateBg.setVisibility(VISIBLE);
                mPSTNImage.setVisibility(GONE);
                mPSTNText.setVisibility(VISIBLE);
                mPSTNText.setText(getResources().getString(R.string.pstn_app_cell_text));
                mPSTNText.measure(0, 0);
                mPSTNText.layout(width / 2 - mPSTNText.getMeasuredWidth() / 2, height / 2 - mPSTNText.getMeasuredHeight() / 2, width / 2 + mPSTNText.getMeasuredWidth(), height / 2 + mPSTNText.getMeasuredHeight());
            } else if (audioOnlyState) {
                mBlack40Bg.setVisibility(GONE);
                mCellStateBg.setVisibility(VISIBLE);
                mVideoMuteText.setVisibility(GONE);
                mAudioOnlyText.setGravity(Gravity.CENTER_HORIZONTAL);
                mAudioOnlyText.setText(getResources().getString(R.string.cell_state_audio_only_incall));
                mAudioOnlyText.layout((width - mAudioOnlyText.getMeasuredWidth()) / 2, (height - mAudioOnlyText.getMeasuredHeight()) / 2, (width + mAudioOnlyText.getMeasuredWidth()) / 2, (height + mAudioOnlyText.getMeasuredHeight()) / 2);
            }

            int Size = (int) (height * 0.57);
            int Left = width / 2 - mLoadingImage.getDrawable().getIntrinsicWidth() / 2;
            int Top = height /2 - mLoadingImage.getDrawable().getIntrinsicHeight() / 2;
            mProfileLoadName.setVisibility(GONE);
            mLoadingImage.layout(Left, Top , Left + mLoadingImage.getDrawable().getIntrinsicWidth(), Top + mLoadingImage.getDrawable().getIntrinsicHeight());
        }

        if (isFullScreen && (videoMute || noVideoState || isUsingPSTN || loadingState || audioOnlyState)) {
            mProfileImage.setVisibility((videoMute || noVideoState || audioOnlyState) ? VISIBLE : GONE);
            mProfileName.setVisibility((isUsingPSTN || videoMute || noVideoState || audioOnlyState) ? VISIBLE : GONE);
            mProfileLoadName.setVisibility(loadingState ? VISIBLE : GONE);
            mProfileImage.layout((width / 2 - 30 * density), (height - mProfileName.getMeasuredHeight() - mVideoMuteText.getMeasuredHeight() - 60 * density) / 2 - 10 * density, (width / 2 + 30 * density), (height - mProfileName.getMeasuredHeight() - mVideoMuteText.getMeasuredHeight() - 60 * density) / 2 + 50 * density);
            mPSTNImage.layout((width / 2 - 30 * density), (height - mProfileName.getMeasuredHeight() - mVideoMuteText.getMeasuredHeight() - 60 * density) / 2 - 10 * density, (width / 2 + 30 * density), (height - mProfileName.getMeasuredHeight() - mVideoMuteText.getMeasuredHeight() - 60 * density) / 2 + 50 * density);
            if (noVideoState) {
                mProfileImage.layout((width / 2 - 30 * density), (height - mProfileName.getMeasuredHeight() - mNoVideoText.getMeasuredHeight() - 60 * density) / 2 - 10 * density, (width / 2 + 30 * density), (height - mProfileName.getMeasuredHeight() - mNoVideoText.getMeasuredHeight() - 60 * density) / 2 + 50 * density);
            }
        } else {
            mProfileImage.setVisibility((observerMode || addOtherState) ? VISIBLE : GONE);
            mProfileLoadName.setVisibility((observerMode || addOtherState) ? VISIBLE : GONE);
            int piSize = (int) (height * 0.57);
            int piLeft = width / 2 - piSize / 2;
            int piTop = gap * 2;
            mProfileImage.layout(piLeft, piTop, piLeft + piSize, piTop + piSize);
            mProfileName.setVisibility((observerMode || addOtherState) ? VISIBLE : GONE);
            if (addOtherState) {
                mImageTurn.layout(piLeft - 5, piTop - 5, piLeft + piSize + 5, piTop + piSize + 5);
                mImageTurn.startAnimation(operatingAnim);
            } else {
                mImageTurn.clearAnimation();
                mImageTurn.setVisibility(GONE);
            }

            if (cancelAddother) {
                int liLeft = width - mCancelAddotherImage.getDrawable().getIntrinsicWidth() * 3 / 4;
                int liTop = -mCancelAddotherImage.getDrawable().getIntrinsicWidth() / 5;
                mCancelAddotherImage.layout(liLeft, liTop, liLeft + mCancelAddotherImage.getDrawable().getIntrinsicWidth(), liTop + mCancelAddotherImage.getDrawable().getIntrinsicHeight());
                mCancelAddotherImage.setPadding(-5, -5, -5, -5);
            }
        }
        //bg
        mCellLoadingStateBg.layout(0, 0, r, b);
        mAddOtherFailedBg.layout(0, 0, r, b);
        mAddOtherBg.layout(0, 0, r, b);
        mMutedAudioBg.layout(0, 0, r, b);
        mCellStateBg.layout(0, 0, r, b);
        mBlack40Bg.layout(0, 0, r, b);

    }

    public interface OnCellStateEventListener {
        void onClickCancelAddother(CellStateView state);
    }
}