package com.github.xcfyl.fabriccc.invoker.request;

/**
 * @author 西城风雨楼
 */
public interface IFabricRequest<T> {
    /**
     * 调用该方法发送请求
     */
    T send();
}
