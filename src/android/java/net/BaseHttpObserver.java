package com.xylink.sdk.sample.net;

import android.log.L;

import com.xylink.sdk.sample.utils.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * 基础的网络观测, 拆分Error为 HTTP exception 和 普通的 exception, 并管理Dispose
 * @author zhangyazhou
 * @date 2018/9/20
 */
public abstract class BaseHttpObserver<T> implements Observer<T> {

    private static final String TAG = "BaseHttpObserver";

    private static Map<String, Disposable> disposables = new HashMap<>(16);

    private String disposeKey;

    public static void removeAndDispose(String key) {
        if (disposables.containsKey(key)) {
            Disposable disposable = disposables.get(key);
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
            disposables.remove(key);
        }
    }

    public static void removeAndDisposeAll() {
        Map<String, Disposable> tempMap = new HashMap<>(16);
        tempMap.putAll(disposables);
        disposables.clear();
        Set<String> keys = tempMap.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Disposable disposable = tempMap.get(key);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            iterator.remove();
            tempMap.remove(key);
        }
    }

    public static void addDispose(String key, Disposable disposable) {
        disposables.put(key, disposable);
    }

    public BaseHttpObserver(String disposeKey) {
        this.disposeKey = disposeKey;
    }

    public String getDisposeKey() {
        return disposeKey;
    }

    @Override
    public final void onError(Throwable e) {
        if (e instanceof HttpException) {
            HttpException exception = (HttpException) e;
            ResponseBody errorBody = exception.response().errorBody();
            if (errorBody != null) {
                try {
                    String data = errorBody.string();
                    boolean isJSON = TextUtils.isJSON(data);
                    if (!isJSON) {
                        L.w(TAG, "disposeKey:" +getDisposeKey() + ",error body is not json");
                    }
                    this.onHttpError(exception, data, TextUtils.isJSON(data));
                } catch (IOException e1) {
                    L.e(TAG, "disposeKey:" +getDisposeKey() + ",read error body error:" + e.getMessage());
                    e1.printStackTrace();
                    this.onHttpError(exception, null, false);
                }
            } else {
                this.onHttpError(exception, null, false);
            }
        } else {
            L.e(TAG, "onError,disposeKey:" + getDisposeKey() + "," + e.getMessage());
            this.onException(e);
        }
    }

    @Override
    public final void onNext(T t) {
        if (t == null) {
            this.onNext(t, false);
        } else if (t instanceof Response) {
            try {
                Response response = (Response) t;
                ResponseBody body = response.body();
                if (body == null) {
                    this.onNext(t, false);
                } else {
                    String bodyData = body.string();
                    MediaType mediaType = body.contentType();
                    Response newResponse = response.newBuilder().body(ResponseBody.create(mediaType, bodyData)).build();
                    this.onNext((T) newResponse, TextUtils.isJSON(bodyData));
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.onNext(t, false);
            }
        } else if (t instanceof ResponseBody) {
            try {
                ResponseBody body = (ResponseBody) t;
                String bodyData = body.string();
                MediaType mediaType = body.contentType();
                this.onNext((T) ResponseBody.create(mediaType, bodyData), TextUtils.isJSON(bodyData));
            } catch (IOException e) {
                e.printStackTrace();
                L.i(TAG, "disposeKey:" +getDisposeKey() + ",parse response body: " + e.getMessage());
                this.onNext(t, false);
            }
        } else if (t instanceof String) {
            this.onNext(t, TextUtils.isJSON((String) t));
        } else {
            this.onNext(t, true) ;
        }
    }

    @Override
    public void onComplete() {
        if (disposeKey != null) {
            removeAndDispose(disposeKey);
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (disposeKey != null) {
            addDispose(disposeKey, d);
        }
    }

    /**
     * 此处错误为业务错误回调, HTTP ERROR已经统一处理!
     */
    public abstract void onException(Throwable throwable);

    public abstract void onHttpError(HttpException exception, String errorData, boolean isJSON);

    public abstract void onNext(T t, boolean isJSON);
}
