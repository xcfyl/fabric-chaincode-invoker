package com.github.xcfyl.fabriccc.invoker.handler;

/**
 * @author 西城风雨楼
 */
public interface ResultHandler<T> {
    void handleSuccess(T result);
    void handleFailure(Throwable throwable);
}
