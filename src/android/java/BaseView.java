package com.xylink.sdk.sample;

/**
 * 基础的View类
 * @param <T>
 * @author zhangyazhou
 */
public interface BaseView<T> {

    /**
     * 设置Presenter
     */
    void setPresenter(T presenter);
}
