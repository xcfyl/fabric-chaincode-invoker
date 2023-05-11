package com.github.xcfyl.fabriccc.invoker.request;

/**
 * @author 西城风雨楼
 */
public class DefaultResultHandler implements ResultHandler<Object> {

    @Override
    public void handleSuccess(Object result) {
        System.out.println("执行成功: ");
        System.out.println(result);
    }

    @Override
    public void handleFailure(Throwable throwable) {
        System.out.println("执行失败: ");
        System.out.println(throwable.getMessage());
    }
}
