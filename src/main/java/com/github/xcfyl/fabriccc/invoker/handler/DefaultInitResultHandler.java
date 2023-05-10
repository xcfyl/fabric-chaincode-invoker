package com.github.xcfyl.fabriccc.invoker.handler;

/**
 * @author 西城风雨楼
 */
public class DefaultInitResultHandler implements ResultHandler<String> {

    @Override
    public void handleSuccess(String result) {
        System.out.println("执行成功: ");
        System.out.println(result);
    }

    @Override
    public void handleFailure(Throwable throwable) {
        System.out.println("执行失败: ");
        System.out.println(throwable.getMessage());
    }
}
