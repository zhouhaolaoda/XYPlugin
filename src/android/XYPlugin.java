package com.plugin.XYPlugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ainemo.sdk.otf.LoginResponseData;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.Settings;
import com.ainemo.sdk.otf.ConnectNemoCallback;
import com.ainemo.sdk.otf.MakeCallResponse;

import com.xylink.sdk.sample.XyCallActivity;
import com.xylink.sdk.sample.IncomingCallService;


public class XYPlugin extends CordovaPlugin {
    //初始化小鱼SDK实例
    private NemoSDK nemoSDK = NemoSDK.getInstance();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // Toast.makeText(this.cordova.getActivity(), action, Toast.LENGTH_LONG).show();
        if (action.equals("initXY")){
            this.initXY(this.webView.getContext(),args.getString(0));
            return true;
        }
        if(action.equals("loginExternal")){
            this.loginExternal(args.getString(0), args.getString(1), callbackContext);
            return true;
        }
        if(action.equals("loginXYlinkAccount")){
            this.loginXYlinkAccount(args.getString(0), args.getString(1), callbackContext);
            return true;
        }
        if(action.equals("loginOut")){
            this.loginOut();
            return true;
        }
        if(action.equals("joinMeeting")){
            this.joinMeeting(args.getString(0), args.getString(1), callbackContext);
            return true;
        }
        if(action.equals("inComeing")){
            this.inComeing();
            return true;
        }
        return false;
    }


    //初始化插件
    public void initXY(Context context, String extId){
        Settings settings = new Settings(extId);
        nemoSDK.init(context,settings);
    }


    //匿名登录
    public void loginExternal(String displayName, String externalUserId, CallbackContext callbackContext){
        try {
            nemoSDK.loginExternalAccount(displayName, externalUserId, new ConnectNemoCallback() {
                //失败回调  errorCode：错误码
                @Override
                public void onFailed(int errorCode) {
                    callbackContext.error(errorCode);
                }
                //成功回调  resp：成功回调数据  isDetectingNetworkTopology：是否需要网络探测
                @Override
                public void onSuccess(LoginResponseData resp, boolean isDetectingNetworkTopology) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("userId",resp.getUserId()+"");
                    map.put("deviceId",resp.getDeviceId()+"");
                    map.put("callNumber",resp.getCallNumber());
                    map.put("deviceUri",resp.getDeviceUri());
                    map.put("deviceDisplayName",resp.getDeviceDisplayName());

                    JSONObject json = new JSONObject(map);

                    callbackContext.success(json);
                }
                //网络探测结束回调  resp：成功回调数据
                @Override
                public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                }
            });
        } catch (Exception e) {
            Toast.makeText(this.cordova.getActivity(), "匿名登录方法执行异常", Toast.LENGTH_LONG).show();
        }
    }


    //账号登录
    public void loginXYlinkAccount(String userName, String passWord, CallbackContext callbackContext){
        try {
            nemoSDK.loginXYlinkAccount(userName, passWord, new ConnectNemoCallback() {
                //失败回调  errorCode：错误码
                @Override
                public void onFailed(int errorCode) {
                    callbackContext.error(errorCode);
                }
                //成功回调  resp：成功回调数据  isDetectingNetworkTopology：是否需要网络探测
                @Override
                public void onSuccess(LoginResponseData resp, boolean isDetectingNetworkTopology) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("userId",resp.getUserId()+"");
                    map.put("deviceId",resp.getDeviceId()+"");
                    map.put("callNumber",resp.getCallNumber());
                    map.put("deviceUri",resp.getDeviceUri());
                    map.put("deviceDisplayName",resp.getDeviceDisplayName());

                    JSONObject json = new JSONObject(map);

                    callbackContext.success(json);
                }
                //网络探测结束回调  resp：成功回调数据
                @Override
                public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                }
            });
        } catch (Exception e) {
            Toast.makeText(this.cordova.getActivity(), "账号登录方法执行异常", Toast.LENGTH_LONG).show();
        }
    }


    //退出登录
    public void loginOut(){
        nemoSDK.logout();
    }


    //加入会议
    public void joinMeeting(String number, String password, CallbackContext callbackContext){
        final CordovaPlugin that = this;        

        nemoSDK.makeCall(number, password, new MakeCallResponse() {
            @Override
            public void onCallSuccess() {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        Intent intentXY = new Intent(that.cordova.getActivity(), XyCallActivity.class);
                        intentXY.putExtra("number", number);
                        that.cordova.getActivity().startActivity(intentXY);
                        callbackContext.success("makeCallSuccess");
                    }
                });
            }

            @Override
            public void onCallFail(int error, String msg) {
                callbackContext.error(msg);
            }
        });
    }
    

    //来电监听
    public void inComeing(){
        Intent incomingCallService = new Intent(this.cordova.getActivity(), IncomingCallService.class);
        this.cordova.getActivity().startService(incomingCallService);
    }
}
