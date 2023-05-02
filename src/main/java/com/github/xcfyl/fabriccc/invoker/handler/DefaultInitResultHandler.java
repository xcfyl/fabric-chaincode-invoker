package com.github.xcfyl.fabriccc.invoker.handler;

/**
 * @author 西城风雨楼
 */
public class DefaultInitResultHandler implements ResultHandler<String> {
    @Override
    public void handle(String result, Throwable e) {
        if (e == null) {
            System.out.println("初始化成功: " + result);
        } else {
            System.out.println("初始化失败: " + e.getMessage());
        }
    }
}
