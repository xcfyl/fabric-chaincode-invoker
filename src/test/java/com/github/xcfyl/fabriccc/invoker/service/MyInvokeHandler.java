package com.github.xcfyl.fabriccc.invoker.service;

import com.github.xcfyl.fabriccc.invoker.handler.ResultHandler;

/**
 * @author 西城风雨楼
 */
public class MyInvokeHandler implements ResultHandler<Person> {
    @Override
    public void handle(Person result, Throwable e) {
        System.out.println("invoke执行的结果是: " + result);
    }
}
