package com.github.xcfyl.fabriccc.invoker.request;

/**
 * @author 西城风雨楼
 */
public interface ResultHandler<T> {
    /**
     * result记录的是链码返回的结果
     * 其实还应该将参数传递过来这是最好的
     *
     * @param result
     */
    void handleSuccess(T result);
    void handleFailure(Throwable throwable);
}
