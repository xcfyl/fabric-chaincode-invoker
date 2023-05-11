package com.github.xcfyl.fabriccc.invoker.annotation;

import com.github.xcfyl.fabriccc.invoker.request.DefaultResultHandler;
import com.github.xcfyl.fabriccc.invoker.request.ResultHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 西城风雨楼
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Invoke {
    int timeout() default 10000;
    // 结果处理器，因为Invoke调用是异步的
    Class<? extends ResultHandler<?>> resultHandler() default DefaultResultHandler.class;
    Class<?> genericClass() default Object.class;
}
