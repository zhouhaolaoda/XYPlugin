package com.xylink.sdk.sample.net;

import android.log.L;

import com.ainemo.util.JsonUtil;

import retrofit2.HttpException;

/**
 * @author zhangyazhou
 * @date 2018/10/10
 */
public class DefaultHttpObserver<T> extends BaseHttpObserver<T> {

    private static final String TAG = "DefaultHttpObserver";

    public DefaultHttpObserver(String disposeKey) {
        super(disposeKey);
    }

    @Override
    public void onException(Throwable throwable) {
        L.e(TAG, "onException:" + throwable.getMessage());
    }

    @Override
    public void onHttpError(HttpException exception, String errorData, boolean isJSON) {
        L.e(TAG, "onHttpError:" + exception.getMessage());
    }

    @Override
    public void onNext(T o, boolean isJSON) {
        L.i(TAG, "onNext:" + JsonUtil.toJson(o));
    }
}
