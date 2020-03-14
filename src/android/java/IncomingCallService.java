package com.xylink.sdk.sample;

import android.app.Service;
import android.content.Intent;
import android.log.L;
import android.os.IBinder;

import com.ainemo.sdk.otf.NemoSDK;

/**
 * 来电界面
 * @author zhangyazhou
 */
public class IncomingCallService extends Service {

    private static final String TAG = "IncomingCallService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NemoSDK.getInstance().setNemoReceivedCallListener((name, number, callIndex) -> {

            L.i(TAG, "onReceivedCall called, remoteName="+name+", remoteNumber="+number+", callIndex="+callIndex);

            Intent it = new Intent(IncomingCallService.this, XyCallActivity.class);
            // 来电名称
            it.putExtra("callerName", name);
            // 来电号码
            it.putExtra("callerNumber", number);
            it.putExtra("callIndex", callIndex);
            it.putExtra("isIncomingCall", true);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
