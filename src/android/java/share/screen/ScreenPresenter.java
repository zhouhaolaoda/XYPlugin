package com.xylink.sdk.sample.share.screen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.log.L;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.ainemo.sdk.otf.NemoSDK;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

import com.xylink.sdk.sample.share.SharingValues;
import com.xylink.sdk.sample.utils.ActivityUtils;
import com.xylink.sdk.sample.view.CustomAlertDialog;

import vulture.module.call.nativemedia.NativeDataSourceManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenPresenter {
    private static final String TAG = "ScreenPresenter";
    private Activity activity;

    private boolean isSharingScreen = false;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private RecordService.ImageReaderCallBack imageReaderCallBack;

    private boolean isBind;

    public ScreenPresenter(Activity context) {
        this.activity = context;
        projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public boolean isSharingScreen() {
        return isSharingScreen;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SharingValues.REQUEST_SHARE_SCREEN) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            Intent intent = new Intent(activity, RecordService.class);
            isBind = activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            NemoSDK.getInstance().dualStreamStart(SharingValues.TYPE_SHARE_SCREEN);
        } else if (requestCode == SharingValues.REQUEST_FLOAT_PERMISSION) {
            if (recordService != null) {
                if (recordService.isRunning()) {
                    L.i(TAG, "recordService is isRunning");
                    recordService.setReaderCallBack(null);
                    recordService.stopRecord();
                } else {
                    Intent permissionIntent = projectionManager.createScreenCaptureIntent();
                    activity.startActivityForResult(permissionIntent, SharingValues.REQUEST_SHARE_SCREEN);
                }
            } else {
                L.i(TAG, "recordService is null");
            }

        }
    }

    public void startShare() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new AlertDialog.Builder(activity)
                    .setTitle("Error")
                    .setMessage("Share screen only works on Android 6.0 or later.")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        } else {
            new CustomAlertDialog(activity).builder()
                    .setTitle("开启权限")
                    .setMsg(activity.getString(R.string.share_screen_permission_tips))
                    .setPositiveButton(activity.getString(R.string.sure), v1 -> {
                        // get float permission: when go back to home show a float view on the home screen
                        if (!Settings.canDrawOverlays(activity)) {
                            activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"
                                    + activity.getPackageName())), SharingValues.REQUEST_FLOAT_PERMISSION);
                        } else {
                            gotPermissionStartShare();
                        }
                    })
                    .setNegativeButton(activity.getString(R.string.cancel), v12 -> {
                    }).setCancelable(false).show();
        }
    }

    public void gotPermissionStartShare() {
        L.i("wang start share");
        Intent permissionIntent = projectionManager.createScreenCaptureIntent();
        activity.startActivityForResult(permissionIntent, SharingValues.REQUEST_SHARE_SCREEN);
    }

    /**
     * stop share and unbind server
     */
    public void stopShare() {
        hideFloatView();
        onDestroy();
        isSharingScreen = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showFloatView() {
        // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
        if (recordService != null && !ActivityUtils.isAppForeground(activity)) {
            recordService.showFloatingWindow();
            recordService.setArbitraryRes(true);
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
        }

        initImageReaderCallBack();
        isSharingScreen = true;
    }

    public void hideFloatView() {
        if (recordService != null) {
            recordService.hideFloatingWindow();
        }
    }

    /**
     * 录屏service connection
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();

            //initImageReaderCallBack();

            recordService.setFloatViewClickListener(new RecordService.OnFloatViewClickListener() {
                @Override
                public void onStop() {
                    NemoSDK.getInstance().dualStreamStop(SharingValues.TYPE_SHARE_SCREEN);
                    ActivityUtils.moveTaskToFront(activity);
                }

                @Override
                public void goBack() {
                    hideFloatView();
                    ActivityUtils.moveTaskToFront(activity);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    /**
     * 初始化图像数据
     */
    private void initImageReaderCallBack() {
        if (recordService == null) {
            return;
        }
        if (imageReaderCallBack == null) {
            imageReaderCallBack = (frame, width, height, pixelStride, rowStride) -> {
                try {
                    if (recordService != null) {
                        String localSourceId = NemoSDK.getInstance().getDataSourceId();
                        L.i(TAG, "refreshData callBack:" + localSourceId + "  width:" + width + ", height:" + height + ", pixelStride=" + pixelStride + ", rowStride : " + rowStride);
                        if (localSourceId != null) {
                            NativeDataSourceManager.putContentData2(localSourceId, frame, frame.length, width, height, pixelStride, rowStride, 0, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }
        if (recordService.getReaderCallBack() == null) {
            recordService.setReaderCallBack(imageReaderCallBack);
        }
    }

    public void onDestroy() {
        try {
            if (recordService != null) {
                recordService.setReaderCallBack(null);
                recordService.stopRecord();
                recordService.unbindService(connection);
                recordService = null;
            }
        } catch (Exception e) {
            Log.i("onDestroy", e.getMessage());
        }

        if (projectionManager != null && connection != null && activity != null) {
            if (isBind) {
                activity.unbindService(connection);
                isBind = false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onStop() {
        showFloatView();
    }

}
