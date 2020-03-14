package com.xylink.sdk.sample.share.screen;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.log.L;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

import java.nio.ByteBuffer;

public class RecordService extends IntentService {

    private static final String TAG = "RecordService";

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private OrientationEventListener orientationListener;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private OnFloatViewClickListener listener;

    //是否正在录屏
    private boolean isRunning;
    //是否任意分辨率
    private boolean arbitraryRes = true;
    private int screenOritation;
    private int width = 1920;
    private int height = 1080;
    private int dpi;

    private long recordFrames;

    private Surface mSurface;

    private Handler handler;
    private ImageReaderCallBack readerCallBack;
    ImageReader imageReader;

    private static final int CALL_BACK_MSG_ID = 1;
    private static final int CALL_BACK_STOP_ID = 2;
    private static final int MSG_RESTART_RECORD = 3;
    private static final int MSG_ORIENTATION_CHANGED = 4;

    private View view;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RecordService() {
        super("RecordService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i(TAG, "onStartCommand Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.i(TAG, "onCreate");
        handler = new Handler(msg -> {
            switch (msg.what) {
                case CALL_BACK_MSG_ID:
                    if (lastData != null && readerCallBack != null) {
                        //L.i(TAG, "before.callback");
                        readerCallBack.callBack(lastData, width, height, msg.arg1, msg.arg2);
                        //L.i(TAG, "after.callback");
                        //handler.sendEmptyMessageDelayed(CALL_BACK_MSG_ID, 500);
                        handler.sendMessageDelayed(handler.obtainMessage(CALL_BACK_MSG_ID, msg.arg1, msg.arg2), 100);
                        handler.sendEmptyMessageDelayed(MSG_ORIENTATION_CHANGED, 200);
                    }
                    break;
                case CALL_BACK_STOP_ID:
                    handler.removeMessages(CALL_BACK_MSG_ID);
                    break;
                case MSG_RESTART_RECORD:
                    startRecord();
                    break;
                case MSG_ORIENTATION_CHANGED:
                    checkOrientationChanged();
                    break;
                default:
                    L.i(TAG, "not handle msg:" + msg.what);
            }

            return true;
        });

        orientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                handler.sendEmptyMessageDelayed(MSG_ORIENTATION_CHANGED, 100);
            }
        };

        orientationListener.enable();

        initView();
    }

    private void initView() {
        L.i("wang server start onCreate");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 500;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 300;
        layoutParams.y = 300;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            view = inflater.inflate(R.layout.layout_float_share_screen, null);
            windowManager.addView(view, layoutParams);
            view.setOnTouchListener(new FloatingOnTouchListener());
            view.performClick();

            // stop share
            view.findViewById(R.id.ll_floating_window_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onStop();
                    }
                }
            });

            // go app
            view.findViewById(R.id.iv_gomain_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.goBack();
                    }
                }
            });
            view.setVisibility(View.GONE);
        }
    }

    private void checkOrientationChanged() {
        handler.removeMessages(MSG_ORIENTATION_CHANGED);
        if (screenOritation != getResources().getConfiguration().orientation) {
            screenOritation = getResources().getConfiguration().orientation;
            L.i(TAG, "onOrientationChanged, orientation : " + screenOritation);
            if (isRunning() && arbitraryRes) {
                pauseRecord();
                restartRecord();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        super.onDestroy();
        L.i(TAG, "onDestroy");
        if (imageReader != null) {
            imageReader.close();
        }

        orientationListener.disable();
    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public void setArbitraryRes(boolean arbitrary) {
        L.i(TAG, "setArbitraryRes, oldArbitraryRes : " + arbitraryRes + ", newArbitraryRes : " + arbitrary);
        arbitraryRes = arbitrary;
    }

    public void handleArbitraryResChanged(boolean arbitrary) {
        L.i(TAG, "handleArbitraryResChanged, oldArbitraryRes : " + arbitraryRes + ", newArbitraryRes : " + arbitrary);
        if (arbitraryRes != arbitrary) {
            arbitraryRes = arbitrary;
            if (isRunning()) {
                pauseRecord();
                restartRecord();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void startRecord() {
        L.i(TAG, "startRecord, isRunning : " + isRunning);

        if (mediaProjection != null && !isRunning) {
            createVirtualDisplay();
            isRunning = true;
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopRecord() {
        L.i(TAG, "stopRecord, isRunning : " + isRunning);

        if (handler != null) {
            handler.removeMessages(CALL_BACK_MSG_ID);
            handler.sendEmptyMessage(CALL_BACK_STOP_ID);
        }

        if (mediaProjection != null && isRunning) {
            virtualDisplay.release();
            mediaProjection.stop();
            isRunning = false;
            lastData = null;
            recordFrames = 0;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void pauseRecord() {
        L.i(TAG, "pauseRecord");

        if (handler != null) {
            handler.removeMessages(CALL_BACK_MSG_ID);
            handler.sendEmptyMessage(CALL_BACK_STOP_ID);
        }

        if (isRunning) {
            virtualDisplay.release();
            isRunning = false;
            lastData = null;
            recordFrames = 0;
        }
    }

    private void restartRecord() {
        L.i(TAG, "restartRecord");
        if (handler != null) {
            handler.removeMessages(MSG_RESTART_RECORD);
            handler.sendEmptyMessage(MSG_RESTART_RECORD);
        }
    }

    private void computeScreenSize() {

        //任意分辨率需求
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);

        if (arbitraryRes) {
            width = dm.widthPixels;
            height = dm.heightPixels;
        } else {
            width = Math.max(dm.widthPixels, dm.heightPixels);
            height = Math.min(dm.widthPixels, dm.heightPixels);
        }

        dpi = (int) dm.density;

        L.i(TAG, "computeCaptureSize, width : " + width + ", height : " + height);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay() {
        computeScreenSize();
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3);
        mSurface = imageReader.getSurface();
        recordFrames = 0;
        imageReader.setOnImageAvailableListener(this::outputImageReader, handler);

        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, new VirtualDisplay.Callback() {
            @Override
            public void onPaused() {
                super.onPaused();
            }

            @Override
            public void onResumed() {
                super.onResumed();
            }

            @Override
            public void onStopped() {
                super.onStopped();
            }
        }, handler);
    }

    private byte[] lastData;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void outputImageReader(ImageReader mImageReader) {

        try {
            //L.i(TAG, "before.acquireLatestImage");
            Image image = mImageReader.acquireLatestImage();

            if (image == null || image.getPlanes() == null || image.getPlanes()[0].getBuffer() == null) {
                return;
            }

            handler.removeMessages(CALL_BACK_MSG_ID);

            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer frame = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();

            //横竖屏切换过程存在宽高比与stride不匹配的情况,丢掉前两帧帧数据
            if (recordFrames++ <= 1) {
                image.close();
                L.i(TAG, "Discard the first two frame because of width & height & stride maybe not matched, width : " + width + ", height : " + height + ", rowStride : " + rowStride);
                return;
            }

            byte[] newData = new byte[frame.capacity()];

            frame.get(newData);

            lastData = newData;
            image.close();
            handler.sendMessage(handler.obtainMessage(CALL_BACK_MSG_ID, pixelStride, rowStride));
        } catch (Exception e) {
            L.i(TAG, e.getMessage());
        }
    }

    public void setReaderCallBack(ImageReaderCallBack callBack) {
        this.readerCallBack = callBack;
    }

    public ImageReaderCallBack getReaderCallBack() {
        return readerCallBack;
    }

    public class RecordBinder extends Binder {
        public RecordService getRecordService() {
            return RecordService.this;
        }
    }

    public interface OnFloatViewClickListener {
        void onStop();

        void goBack();
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            L.i("wang: onTouch: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public void setFloatViewClickListener(OnFloatViewClickListener listener) {
        this.listener = listener;
    }

    public void showFloatingWindow() {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void hideFloatingWindow() {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    public interface ImageReaderCallBack {
        void callBack(byte[] reader, int width, int height, int pixelStride, int rowStride);
    }
}