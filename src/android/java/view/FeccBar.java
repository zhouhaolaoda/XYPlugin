package com.xylink.sdk.sample.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.log.L;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ainemo.shared.UserActionListener;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

public class FeccBar extends RelativeLayout {
    private static final String TAG = "FeccBar";
    private UserActionListener actionListener;
    private Context context;
    private ImageButton mFeccLeftBtn;
    private ImageButton mFeccRightBtn;
    private ImageButton mFeccUpBtn;
    private ImageButton mFeccDownBtn;
    private ImageView mFeccPanView;
    private ImageView mFeccControlBg;
    private ImageView mFeccControlBgLeft;
    private ImageView mFeccControlBgRight;
    private ImageView mFeccControlBgUp;
    private ImageView mFeccControlBgDown;
    private RelativeLayout mFeccControl;

    private ImageView zoomInAdd;
    private ImageView zoomInPlus;

    private boolean feccHorizontalControl = false;
    private boolean feccVerticalControl = false;

    private boolean isActionMoveLeft;
    private boolean isActionMoveRight;
    private boolean isActionMoveUp;
    private boolean isActionMoveDown;
    private int lastFeccCommand = UserActionListener.USER_ACTION_FECC_STOP;

    public FeccBar(Context context) {
        super(context);
        init(context);
    }

    public FeccBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setFeccListener(UserActionListener listener) {
        this.actionListener = listener;
    }

    private void init(Context context) {
        this.context = context;
        View feccView = LayoutInflater.from(context).inflate(R.layout.layout_fecc_control, this);
        mFeccLeftBtn = feccView.findViewById(R.id.fecc_left);
        mFeccRightBtn = feccView.findViewById(R.id.fecc_right);
        mFeccUpBtn = feccView.findViewById(R.id.fecc_up);
        mFeccDownBtn = feccView.findViewById(R.id.fecc_down);
        mFeccControl = feccView.findViewById(R.id.fecc_control);
        mFeccControlBg = feccView.findViewById(R.id.fecc_control_bg);
        mFeccControlBgLeft = feccView.findViewById(R.id.fecc_control_bg_left);
        mFeccControlBgRight = feccView.findViewById(R.id.fecc_control_bg_right);
        mFeccControlBgUp = feccView.findViewById(R.id.fecc_control_bg_up);
        mFeccControlBgDown = feccView.findViewById(R.id.fecc_control_bg_down);
        mFeccPanView = feccView.findViewById(R.id.fecc_pan);

        zoomInAdd = feccView.findViewById(R.id.zoom_in_add);
        zoomInPlus = feccView.findViewById(R.id.zoom_out_plus);
    }

    private float GetFeccBtnPositon(ImageButton feccButton) {
        float animator = 0f;

        if (feccButton == mFeccRightBtn) {
            animator = mFeccRightBtn.getRight() - mFeccPanView.getWidth() + 30;
        } else if (feccButton == mFeccLeftBtn) {
            animator = mFeccLeftBtn.getX();
        } else if (feccButton == mFeccUpBtn) {
            animator = mFeccUpBtn.getY();
        } else if (feccButton == mFeccDownBtn) {
            animator = mFeccDownBtn.getBottom() - mFeccPanView.getHeight() + 30;
        }
        return animator;
    }

    private void FeccPanTurnSide(final ImageButton feccButton) {
        float animator = GetFeccBtnPositon(feccButton);
        ObjectAnimator fadeIn = null;
        if (feccButton == mFeccLeftBtn) {
            mFeccControlBgLeft.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", animator);
        } else if (feccButton == mFeccRightBtn) {
            mFeccControlBgRight.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", animator);
        } else if (feccButton == mFeccUpBtn) {
            mFeccControlBgUp.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", animator);
        } else if (feccButton == mFeccDownBtn) {
            mFeccControlBgDown.setVisibility(View.VISIBLE);
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", animator);
        }

        fadeIn.setDuration(100);
        fadeIn.start();
        mFeccControlBg.setVisibility(View.VISIBLE);
    }

    private void FeccPanTurnPingPong(final ImageButton feccButton) {
        float animator = GetFeccBtnPositon(feccButton);
        ObjectAnimator fadeIn = null;
        if (feccButton == mFeccLeftBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", mFeccPanView.getLeft(), animator, mFeccPanView.getLeft());
            fadeIn.setDuration(200);
        } else if (feccButton == mFeccRightBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", mFeccPanView.getLeft(), animator, mFeccPanView.getLeft());
            fadeIn.setDuration(200);
        } else if (feccButton == mFeccUpBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", mFeccPanView.getTop(), animator, mFeccPanView.getTop());
            fadeIn.setDuration(200);
        } else if (feccButton == mFeccDownBtn) {
            fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "y", mFeccPanView.getTop(), animator, mFeccPanView.getTop());
            fadeIn.setDuration(200);
        }


        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                if (feccButton == mFeccLeftBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgLeft.setVisibility(View.VISIBLE);
                } else if (feccButton == mFeccRightBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgRight.setVisibility(View.VISIBLE);
                } else if (feccButton == mFeccUpBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgUp.setVisibility(View.VISIBLE);
                } else if (feccButton == mFeccDownBtn) {
                    mFeccControlBg.setVisibility(View.VISIBLE);
                    mFeccControlBgDown.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mFeccControlBg.setVisibility(View.VISIBLE);
                if (feccButton == mFeccLeftBtn) {
                    mFeccControlBgLeft.setVisibility(View.GONE);
                } else if (feccButton == mFeccRightBtn) {
                    mFeccControlBgRight.setVisibility(View.GONE);
                } else if (feccButton == mFeccUpBtn) {
                    mFeccControlBgUp.setVisibility(View.GONE);
                } else if (feccButton == mFeccDownBtn) {
                    mFeccControlBgDown.setVisibility(View.GONE);
                }
            }
        });
        fadeIn.start();

    }

    private void FeccPanTurnOrigin() {
        float animatorx = 0f;
        float animatory = 0f;
        animatorx = mFeccPanView.getLeft();
        animatory = mFeccPanView.getTop();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mFeccPanView, "x", animatorx);
        fadeIn.setDuration(100);
        fadeIn.start();

        ObjectAnimator fadeIny = ObjectAnimator.ofFloat(mFeccPanView, "y", animatory);
        fadeIny.setDuration(100);
        fadeIny.start();

        mFeccControlBg.setVisibility(View.VISIBLE);
        mFeccControlBgUp.setVisibility(View.GONE);
        mFeccControlBgDown.setVisibility(View.GONE);
        mFeccControlBgRight.setVisibility(View.GONE);
        mFeccControlBgLeft.setVisibility(View.GONE);
    }

    public void initFeccEventListeners() {
        createFeccBtnGestureDetector(mFeccLeftBtn, UserActionListener.USER_ACTION_FECC_LEFT, UserActionListener.USER_ACTION_FECC_STEP_LEFT);
        createFeccBtnGestureDetector(mFeccRightBtn, UserActionListener.USER_ACTION_FECC_RIGHT, UserActionListener.USER_ACTION_FECC_STEP_RIGHT);
        createFeccBtnGestureDetector(mFeccUpBtn, UserActionListener.USER_ACTION_FECC_UP, UserActionListener.USER_ACTION_FECC_STEP_UP);
        createFeccBtnGestureDetector(mFeccDownBtn, UserActionListener.USER_ACTION_FECC_DOWN, UserActionListener.USER_ACTION_FECC_STEP_DOWN);
        if (zoomInAdd != null) {

            createZoomInGestureDetector(zoomInAdd);
        }
        if (zoomInPlus != null) {

            createZoomOutGestureDetector(zoomInPlus);
        }
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_LEFT, UserActionListener.USER_ACTION_FECC_STEP_LEFT);
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_RIGHT, UserActionListener.USER_ACTION_FECC_STEP_RIGHT);
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_UP, UserActionListener.USER_ACTION_FECC_STEP_UP);
        createFeccPanGestureDetector(mFeccControlBg, mFeccPanView, UserActionListener.USER_ACTION_FECC_DOWN, UserActionListener.USER_ACTION_FECC_STEP_DOWN);
    }


    private void createFeccBtnGestureDetector(final ImageButton feccButton, final int actionTurn, final int actionStep) {
        final GestureDetector mGestureDetector = new GestureDetector(feccButton.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (!feccButton.isClickable()) {
                    return;
                }
                sendFeccCommand(actionTurn);
                FeccPanTurnSide(feccButton);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (feccButton.isClickable()) {
                    sendFeccCommand(actionStep);
                    FeccPanTurnPingPong(feccButton);
                }

                return true;
            }

        });

        feccButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!v.isClickable() || mGestureDetector.onTouchEvent(event))
                    return true;

                switch ((event.getAction() & MotionEvent.ACTION_MASK)) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        FeccPanTurnOrigin();
                        if (actionTurn == UserActionListener.USER_ACTION_FECC_LEFT
                                || actionTurn == UserActionListener.USER_ACTION_FECC_RIGHT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_LEFT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_RIGHT) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_STOP);
                        } else if (actionTurn == UserActionListener.USER_ACTION_FECC_UP
                                || actionTurn == UserActionListener.USER_ACTION_FECC_DOWN
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_UP
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_DOWN) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP);
                        }
                        return true;
                }
                return true;
            }
        });
    }

    private void sendFeccCommand(int command) {
        if (actionListener != null) {
            actionListener.onUserAction(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP, null);
            actionListener.onUserAction(UserActionListener.USER_ACTION_FECC_STOP, null);
            lastFeccCommand = command;
            actionListener.onUserAction(command, null);
        }
    }

    private void createZoomInGestureDetector(ImageView zoomInAdd) {
        GestureDetector zoomGestureDetector = new GestureDetector(zoomInAdd.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                L.i(TAG, "createZoomInGestureDetector onLongPress...");
                if (actionListener != null) {
                    actionListener.onUserAction(UserActionListener.FECC_ZOOM_IN, null);
                }
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                L.i(TAG, "createZoomInGestureDetector onSingleTapConfirmed...");
                if (actionListener != null) {
                    actionListener.onUserAction(UserActionListener.FECC_STEP_ZOOM_IN, null);
                }
                return true;
            }
        });

        zoomInAdd.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (zoomGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        L.i(TAG, "createZoomInGestureDetector ACTION_UP...");
                        if (actionListener != null) {
                            actionListener.onUserAction(UserActionListener.FECC_ZOOM_TURN_STOP, null);
                        }
                        return true;
                }
                return true;
            }
        });
    }

    private void createZoomOutGestureDetector(ImageView zoomInPlus) {
        GestureDetector zoomGestureDetector = new GestureDetector(zoomInPlus.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                L.i(TAG, "createZoomOutGestureDetector onLongPress...");
                if (actionListener != null) {
                    actionListener.onUserAction(UserActionListener.FECC_ZOOM_OUT, null);
                }
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                L.i(TAG, "createZoomOutGestureDetector onSingleTapConfirmed...");
                if (actionListener != null) {
                    actionListener.onUserAction(UserActionListener.FECC_STEP_ZOOM_OUT, null);
                }
                return true;
            }
        });

        zoomInPlus.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (zoomGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        L.i(TAG, "createZoomOutGestureDetector ACTION_UP...");
                        if (actionListener != null) {
                            actionListener.onUserAction(UserActionListener.FECC_ZOOM_TURN_STOP, null);
                        }
                        return true;
                }
                return true;
            }
        });
    }

    private void createFeccPanGestureDetector(final ImageView feccBigCircle, final ImageView feccSmallCircle, final int actionTurn, final int actionStep) {
        feccBigCircle.setLongClickable(true);
        feccBigCircle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isActionMoveLeft = false;
                        isActionMoveRight = false;
                        isActionMoveUp = false;
                        isActionMoveDown = false;
                        mFeccPanView.setImageResource(R.drawable.fecc_middle_icon);
                        FeccPanTurnOrigin();
                        sendFeccCommand(UserActionListener.USER_ACTION_FECC_STOP);
                        sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP);
                        if (actionTurn == UserActionListener.USER_ACTION_FECC_LEFT
                                || actionTurn == UserActionListener.USER_ACTION_FECC_RIGHT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_LEFT
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_RIGHT) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_STOP);
                        } else if (actionTurn == UserActionListener.USER_ACTION_FECC_UP
                                || actionTurn == UserActionListener.USER_ACTION_FECC_DOWN
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_UP
                                || actionStep == UserActionListener.USER_ACTION_FECC_STEP_DOWN) {
                            sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP);
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        isActionMoveLeft = false;
                        isActionMoveRight = false;
                        isActionMoveUp = false;
                        isActionMoveDown = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() >= 1) {
                            mFeccPanView.setImageResource(R.drawable.fecc_middle_icon_press);
                            int bigR = feccBigCircle.getWidth() / 2;
                            int smallR = feccSmallCircle.getWidth() / 2;
                            int r = bigR - smallR;

                            float bigRPivotX = feccBigCircle.getPivotX();
                            float bigRPivotY = feccBigCircle.getPivotY();


                            float eventx = event.getX(0);
                            float eventy = event.getY(0);

                            float absRelX = Math.abs(eventx - bigR);
                            float absRelY = Math.abs(eventy - bigR);

                            if (eventx > bigRPivotX + bigR - smallR && absRelX > absRelY && feccHorizontalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgRight.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(View.GONE);
                                mFeccControlBgUp.setVisibility(View.GONE);
                                mFeccControlBgDown.setVisibility(View.GONE);
                                if (!isActionMoveRight) {
                                    isActionMoveRight = true;
                                    isActionMoveLeft = false;
                                    isActionMoveUp = false;
                                    isActionMoveDown = false;
                                    sendFeccCommand(UserActionListener.USER_ACTION_FECC_RIGHT);
                                }
                            } else if (eventx < bigRPivotX && absRelX > absRelY && feccHorizontalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(View.VISIBLE);
                                mFeccControlBgRight.setVisibility(View.GONE);
                                mFeccControlBgUp.setVisibility(View.GONE);
                                mFeccControlBgDown.setVisibility(View.GONE);
                                if (!isActionMoveLeft) {
                                    isActionMoveLeft = true;
                                    isActionMoveRight = false;
                                    isActionMoveUp = false;
                                    isActionMoveDown = false;
                                    sendFeccCommand(UserActionListener.USER_ACTION_FECC_LEFT);
                                }
                            } else if (eventy > bigRPivotY + bigR - smallR && absRelY > absRelX && feccVerticalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(View.GONE);
                                mFeccControlBgRight.setVisibility(View.GONE);
                                mFeccControlBgUp.setVisibility(View.GONE);
                                mFeccControlBgDown.setVisibility(View.VISIBLE);
                                if (!isActionMoveDown) {
                                    isActionMoveDown = true;
                                    isActionMoveLeft = false;
                                    isActionMoveRight = false;
                                    isActionMoveUp = false;
                                    sendFeccCommand(UserActionListener.USER_ACTION_FECC_DOWN);
                                }
                            } else if (eventy < bigRPivotY && absRelY > absRelX && feccVerticalControl) {
                                mFeccControlBg.setVisibility(View.VISIBLE);
                                mFeccControlBgLeft.setVisibility(View.GONE);
                                mFeccControlBgRight.setVisibility(View.GONE);
                                mFeccControlBgDown.setVisibility(View.GONE);
                                mFeccControlBgUp.setVisibility(View.VISIBLE);
                                if (!isActionMoveUp) {
                                    isActionMoveUp = true;
                                    isActionMoveLeft = false;
                                    isActionMoveRight = false;
                                    isActionMoveDown = false;
                                    sendFeccCommand(UserActionListener.USER_ACTION_FECC_UP);
                                }
                            }

                            double d = Math.sqrt((eventx - bigR) * (eventx - bigR) + (eventy - bigR) * (eventy - bigR));
                            r += 25; // critical pixel 包含小圆发光距离

                            if (d > r) { // moving out of the big circle
                                float fx = ((float) bigR + ((float) r) * (eventx - (float) bigR) / (float) d);
                                float fy = ((float) bigR + ((float) r) * (eventy - (float) bigR) / (float) d);

                                if (feccHorizontalControl) {
                                    feccSmallCircle.setX(fx - smallR + 15); // FIXME: 2017/10/18 temp fix
                                }
                                if (feccVerticalControl) {
                                    feccSmallCircle.setY(fy - smallR);
                                }
                            } else {  // moving inside of the big circle
                                if (feccHorizontalControl) {
                                    feccSmallCircle.setX(eventx - smallR);
                                }
                                if (feccVerticalControl) {
                                    feccSmallCircle.setY(eventy - smallR);
                                }
                            }
                            invalidate();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void setFECCButtonVisible(final boolean visible) {
        if (mFeccControl != null) {
            mFeccControl.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setZoomInOutVisible(boolean visible) {
        if (zoomInAdd != null && zoomInPlus != null) {
            zoomInPlus.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            zoomInAdd.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            if (mFeccUpBtn != null && mFeccDownBtn != null) {
                if (visible) {
                    mFeccUpBtn.setImageResource(R.drawable.fecc_up);
                    mFeccDownBtn.setImageResource(R.drawable.fecc_down);
                } else {
                    mFeccUpBtn.setImageResource(R.drawable.fecc_up_disabled);
                    mFeccDownBtn.setImageResource(R.drawable.fecc_down_disabled);
                }
            }
        }
    }


    public void setFeccTiltControl(final boolean horizontalStatus, final boolean verticalStatus) {

        feccHorizontalControl = horizontalStatus;
        feccVerticalControl = verticalStatus;

        if (mFeccControlBgLeft != null) {
            mFeccControlBgLeft.setImageResource(R.drawable.fecc_left_bg);
        }
        if (mFeccControlBgRight != null) {
            mFeccControlBgRight.setImageResource(R.drawable.fecc_right_bg);
        }
        if (mFeccControlBgUp != null) {
            mFeccControlBgUp.setImageResource(R.drawable.fecc_up_bg);
        }
        if (mFeccControlBgDown != null) {
            mFeccControlBgDown.setImageResource(R.drawable.fecc_down_bg);
        }

        if (feccHorizontalControl && !feccVerticalControl) {    // only support horizontal
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setImageResource(R.drawable.fecc_up_disabled);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setImageResource(R.drawable.fecc_down_disabled);
            }
            if (mFeccControlBg != null) {
                mFeccControlBg.setImageResource(R.drawable.bg_toolbar_fecc_pan);
            }
        } else {
            if (mFeccControlBg != null) {
                mFeccControlBg.setImageResource(R.drawable.bg_toolbar_fecc_pan);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setImageResource(R.drawable.fecc_down);
            }
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setImageResource(R.drawable.fecc_up);
            }
        }

        if (feccVerticalControl && !feccHorizontalControl) {     // only support vertical
            if (mFeccLeftBtn != null) {
                mFeccLeftBtn.setImageResource(R.drawable.fecc_left_disabled);
                mFeccLeftBtn.setClickable(false);
            }
            if (mFeccRightBtn != null) {
                mFeccRightBtn.setImageResource(R.drawable.fecc_right_disabled);
                mFeccRightBtn.setClickable(false);
            }

        } else {
            if (mFeccLeftBtn != null) {
                mFeccLeftBtn.setImageResource(R.drawable.fecc_left);
                mFeccLeftBtn.setClickable(true);
            }
            if (mFeccRightBtn != null) {
                mFeccRightBtn.setImageResource(R.drawable.fecc_right);
                mFeccRightBtn.setClickable(true);
            }
        }

        if (feccHorizontalControl) {
            if (mFeccLeftBtn != null) {
                mFeccLeftBtn.setVisibility(View.VISIBLE);
            }
            if (mFeccRightBtn != null) {
                mFeccRightBtn.setVisibility(View.VISIBLE);
            }
        }

        if (feccVerticalControl) {
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setVisibility(View.VISIBLE);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setVisibility(View.VISIBLE);
            }
        } else {
            if (mFeccUpBtn != null) {
                mFeccUpBtn.setVisibility(View.VISIBLE);
            }
            if (mFeccDownBtn != null) {
                mFeccDownBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    public boolean isSupportHorizontalFECC(int capability) {
        return (capability & 1 << 1) != 0;
    }

    public boolean isSupportVerticalFECC(int capability) {
        return (capability & 1 << 2) != 0;
    }

    public boolean isSupportZoomInOut(int capability) {
        return (capability & 1 << 4) != 0;
    }

    public void onDestroy() {
        mFeccLeftBtn = null;
        mFeccRightBtn = null;
        mFeccControl = null;
        mFeccControlBgRight = null;
        mFeccControlBgLeft = null;
    }


}
