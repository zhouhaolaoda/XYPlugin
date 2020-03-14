package com.xylink.sdk.sample.share.whiteboard.view;

import android.content.Context;
import android.log.L;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

import com.xylink.sdk.sample.share.whiteboard.message.PenType;
import com.xylink.sdk.sample.view.CustomAlertDialog;

public class WhiteBoardCell extends RelativeLayout {

    private static final String TAG = WhiteBoardCell.class.getSimpleName();

    private static final int YELLOW = 0xffc766;
    private static final int BLACK = 0x181818;
    private static final int BLUE = 0x2081bf;
    private static final int RED = 0xff6666;

    private WhiteBoardTextureView mVideoView;

    private boolean mTouchOutofRange = false;
    private float mPrevX = -1.0f;
    private float mPrevY = -1.0f;

    private boolean isColorSelecting = false;

    private View mWhiteboardDrawToolbar;
    private MuteImageView mWhiteboardPencil;
    private MuteImageView mWhiteboardMarker;
    private MuteImageView mWhiteboardEraser;
    private ImageButton mWhiteboardClearAll;
    private ImageButton mWhiteboardSelectColor;

    private View mWhiteboardColorSelectToolbar;
    private ImageButton mWhiteboardYellow;
    private ImageButton mWhiteboardBlack;
    private ImageButton mWhiteboardBlue;
    private ImageButton mWhiteboardRed;
    private ImageButton mWhiteboardColorBack;
    private WhiteBoardCellListener mListener;

    private boolean fullScreen = false, mDraged = false;

    private Handler mHandler;
    private DisplayState mDisplayState = DisplayState.STOPPED;
    private OnWhiteBoardCellEventListener onWhiteBoardCellEventListener;

    private SurfaceGestureListener mGestureListener = new SurfaceGestureListener();
    private GestureDetector mGestureDetector;
    private int mLastDragX, mLastDragY;

    public void setOnWhiteBoardCellEventListener(OnWhiteBoardCellEventListener onWhiteBoardCellEventListener) {
        this.onWhiteBoardCellEventListener = onWhiteBoardCellEventListener;
    }

    public interface WhiteBoardCellListener {
        void onSlideBarShow();
    }

    public interface OnWhiteBoardCellEventListener {
        void onLongPress(MotionEvent e, WhiteBoardCell cell);

        boolean onDoubleTap(MotionEvent e, WhiteBoardCell cell);

        boolean onSingleTapConfirmed(MotionEvent e, WhiteBoardCell cell);

        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, WhiteBoardCell cell);
    }

    public WhiteBoardCell(Context context) {
        super(context);
        initView(context);
    }

    public WhiteBoardCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WhiteBoardCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        mHandler = new Handler();
        View.inflate(context, R.layout.conversation_whiteboard_cell, this);
        mVideoView = findViewById(R.id.whiteboard_video_view);

        mWhiteboardDrawToolbar = findViewById(R.id.whiteboard_draw_toolbar);
        mWhiteboardColorSelectToolbar = findViewById(R.id.whiteboard_color_select_toolbar);

        mWhiteboardPencil = findViewById(R.id.whiteboard_cell_pencil);
        mWhiteboardMarker = findViewById(R.id.whiteboard_cell_marker);
        mWhiteboardEraser = findViewById(R.id.whiteboard_cell_eraser);

        initGesture();
        mWhiteboardPencil.setOnClickListener(v -> {
            mVideoView.setCurrentLocalPenType(PenType.OPAQUE);
            mWhiteboardPencil.setMuted(false);
            mWhiteboardMarker.setMuted(true);
            mWhiteboardEraser.setMuted(true);
        });

        mWhiteboardMarker.setOnClickListener(v -> {
            mVideoView.setCurrentLocalPenType(PenType.TRANSLUCENT);
            mWhiteboardMarker.setMuted(false);
            mWhiteboardPencil.setMuted(true);
            mWhiteboardEraser.setMuted(true);
        });

        mWhiteboardEraser.setOnClickListener(v -> {
            mVideoView.setCurrentLocalPenType(PenType.ERASER);
            mWhiteboardEraser.setMuted(false);
            mWhiteboardPencil.setMuted(true);
            mWhiteboardMarker.setMuted(true);
        });

        mWhiteboardClearAll = findViewById(R.id.whiteboard_cell_clear);
        mWhiteboardClearAll.setOnClickListener(v -> showClearWhiteBoardConfirmView());

        mWhiteboardSelectColor = findViewById(R.id.whiteboard_color_select);
        mWhiteboardSelectColor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isColorSelecting = true;
                showWhiteboardToolBar(true);
            }
        });

        mWhiteboardYellow = findViewById(R.id.whiteboard_color_yellow);
        mWhiteboardYellow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhiteboardSelectColor.setBackgroundResource(R.drawable.whiteboard_yellow);
                isColorSelecting = false;
                showWhiteboardToolBar(true);
                mVideoView.setLocalColor(YELLOW);
            }
        });

        mWhiteboardBlack = findViewById(R.id.whiteboard_color_black);
        mWhiteboardBlack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhiteboardSelectColor.setBackgroundResource(R.drawable.whiteboard_black);
                isColorSelecting = false;
                showWhiteboardToolBar(true);
                mVideoView.setLocalColor(BLACK);
            }
        });

        mWhiteboardBlue = findViewById(R.id.whiteboard_color_blue);
        mWhiteboardBlue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhiteboardSelectColor.setBackgroundResource(R.drawable.whiteboard_blue);
                isColorSelecting = false;
                showWhiteboardToolBar(true);
                mVideoView.setLocalColor(BLUE);
            }
        });

        mWhiteboardRed = findViewById(R.id.whiteboard_color_red);
        mWhiteboardRed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhiteboardSelectColor.setBackgroundResource(R.drawable.whiteboard_red);
                isColorSelecting = false;
                showWhiteboardToolBar(true);
                mVideoView.setLocalColor(RED);
            }
        });

        mWhiteboardColorBack = findViewById(R.id.whiteboard_color_back);
        mWhiteboardColorBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isColorSelecting = false;
                showWhiteboardToolBar(true);
            }
        });

        mVideoView.setLocalColor(BLUE);
    }


    public View getCellLayout() {
        return this;
    }

    public View getVideoView() {
        return mVideoView;
    }

    public int getViewId() {
        return -1;
    }

    public void onPause() {
        mVideoView.pause();
    }

    public void onResume() {
        mVideoView.resume();

    }

    public void onDestory() {
        if (mVideoView != null) {
            mVideoView.destroy();
            removeView(mVideoView);
            mVideoView = null;
        }
    }

    public void stop() {
        if (null != mVideoView) {
            mVideoView.close();
        }
    }

    public void onMessage(String text) {
        if (null != mVideoView) {
            mVideoView.onMessage(text);
        }
    }

    public void onMessages(Object obj) {
        if (null != mVideoView) {
            mVideoView.onMessages(obj);
        }
    }

    public void switchToDisplayState() {
        onResume();
    }

    public void setWhiteBoardResolution(String prop) {
        String[] s = prop.split("x");
        int width = 1024;
        int height = 768;
        try {
            width = Integer.parseInt(s[0]);
            height = Integer.parseInt(s[1]);
        } catch (Exception ex) {
            width = 1024;
            height = 768;
        }

        if (null != mVideoView) {
            mVideoView.setWhiteBoardResolution(width, height);
        }
    }

    public void setwhiteBoardMeasure(boolean fullScreen, int width, int height) {
    }

    public void setDisplayMode(boolean matchParent) {
    }

    public void setWhiteBoardListener(WhiteBoardTextureView.WhiteBoardViewListener wbListener) {
        if (null != mVideoView) {
            mVideoView.setWhiteBoardViewListener(wbListener);
        }
    }

    public void setWhiteBoardCellListener(WhiteBoardCellListener wbCellListener) {
        this.mListener = wbCellListener;
    }

    public void onTouch(MotionEvent event) {
        if (null != mVideoView) {
            float x = event.getX();
            float y = event.getY();

            int offsetX = (getWidth() - mVideoView.getWidth()) / 2;
            int offsetY = (getHeight() - mVideoView.getHeight()) / 2;

            x -= offsetX;
            y -= offsetY;

            if (x < 0 || x > mVideoView.getWidth() || y < 0 || y > mVideoView.getHeight()) {
                if (!mTouchOutofRange) {
                    mVideoView.onTouch(MotionEvent.ACTION_UP, this.mPrevX, this.mPrevY);
                }
                mTouchOutofRange = true;
                return;
            }

            this.mPrevX = x;
            this.mPrevY = y;
            if (mTouchOutofRange) {
                if (MotionEvent.ACTION_MOVE == event.getAction() || MotionEvent.ACTION_DOWN == event.getAction()) {
                    mVideoView.onTouch(MotionEvent.ACTION_DOWN, x, y);
                }
                mTouchOutofRange = false;
            } else {
                mVideoView.onTouch(event.getAction(), x, y);
            }
        }

    }

    public void show(boolean starting) {
        DisplayState newState = (starting) ? DisplayState.INIT : DisplayState.STARTED;
        if (newState.ordinal() > mDisplayState.ordinal()) {
            DisplayState old = mDisplayState;
            mDisplayState = newState;
            if (starting && DisplayState.INIT == mDisplayState) {
                mHandler.removeCallbacks(changeDisplayStateRunnable);
                mHandler.postDelayed(changeDisplayStateRunnable, 500);
            } else if (!starting && DisplayState.STARTING != old) {
                mHandler.removeCallbacks(changeDisplayStateRunnable);
                showWhiteboardView();
            }
        }
    }

    public void hide() {
        mDisplayState = DisplayState.STOPPED;
        mHandler.removeCallbacks(changeDisplayStateRunnable);
        setVisibility(GONE);
//		mStartingCircle.clearAnimation();
    }

    private Runnable changeDisplayStateRunnable = new Runnable() {
        public void run() {
            L.i("changeDisplayStateRunnable " + mDisplayState);
            if (DisplayState.INIT == mDisplayState) {
                mDisplayState = DisplayState.STARTING;
//				showStartingView();
                mHandler.removeCallbacks(changeDisplayStateRunnable);
                mHandler.postDelayed(this, 1000);
            } else if (DisplayState.STARTING == mDisplayState) {
                mHandler.removeCallbacks(changeDisplayStateRunnable);
                mHandler.postDelayed(this, 1000);
            } else if (DisplayState.STARTED == mDisplayState) {
                showWhiteboardView();
            }
        }
    };

    public void showWhiteboardView() {
        mWhiteboardDrawToolbar.setVisibility(VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        L.i(TAG, "onTouchEvent " + event.toString());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDragX = (int) event.getRawX();
                mLastDragY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastDragX = 0;
                mLastDragY = 0;
                break;
        }
        /// onTouch(event);
        onTouch(event);
        mGestureDetector.onTouchEvent(event);
        ///return !fullScreen;
        return true;
    }

    private void initGesture() {
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);
    }

    public class SurfaceGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float dx = e2.getRawX() - mLastDragX;
            float dy = e2.getRawY() - mLastDragY;
            boolean result = (onWhiteBoardCellEventListener != null) && onWhiteBoardCellEventListener.onScroll(e1, e2, dx, dy, WhiteBoardCell.this);
            mLastDragX = (int) e2.getRawX();
            mLastDragY = (int) e2.getRawY();
            return result;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return (onWhiteBoardCellEventListener != null) && onWhiteBoardCellEventListener.onDoubleTap(e, WhiteBoardCell.this);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return (onWhiteBoardCellEventListener != null) && onWhiteBoardCellEventListener.onSingleTapConfirmed(e, WhiteBoardCell.this);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onWhiteBoardCellEventListener != null) {
                onWhiteBoardCellEventListener.onLongPress(e, WhiteBoardCell.this);
            }
        }

    }

    public void showClearWhiteBoardConfirmView() {
        if (null != mListener) {
            //to notify fragment hide tool bar
            mListener.onSlideBarShow();
        }
        new CustomAlertDialog(getContext()).builder().setTitle(getContext().getString(R.string.clear_white_board_title))
                .setMsg(getContext().getString(R.string.clear_white_board_content))
                .setPositiveButton(getContext().getString(R.string.sure), new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mVideoView.clear();
                    }
                }).setNegativeButton(getContext().getString(R.string.cancel), new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).setCancelable(false).show();
    }

    public void showWhiteboardToolBar(boolean show) {
        if (mWhiteboardDrawToolbar == null || mWhiteboardColorSelectToolbar == null) {
            return;
        }

        L.i(TAG, "showWhiteboardToolBar " + show + " colorSelecting " + isColorSelecting);
        if (show && isColorSelecting) {
            mWhiteboardDrawToolbar.setVisibility(View.GONE);
            mWhiteboardColorSelectToolbar.setVisibility(View.VISIBLE);
        } else if (show && !isColorSelecting) {
            mWhiteboardDrawToolbar.setVisibility(View.VISIBLE);
            mWhiteboardColorSelectToolbar.setVisibility(View.GONE);
        } else {
            mWhiteboardDrawToolbar.setVisibility(View.GONE);
            mWhiteboardColorSelectToolbar.setVisibility(View.GONE);
        }
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public boolean ismDraged() {
        return mDraged;
    }

    public void setmDraged(boolean mDraged) {
        this.mDraged = mDraged;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public void setMeasure(int width, int height) {
        setwhiteBoardMeasure(fullScreen, width, height);
    }

    private enum DisplayState {
        STOPPED,
        INIT,
        STARTING,
        STARTED,
    }
}
