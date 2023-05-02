package com.github.xcfyl.fabriccc.invoker.annotation;

import com.github.xcfyl.fabriccc.invoker.handler.DefaultInitResultHandler;
import com.github.xcfyl.fabriccc.invoker.handler.ResultHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 西城风雨楼
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Init {
    int timeout() default Integer.MAX_VALUE;
    // 初始化也是异步调用，因此这里要设置结果处理器
    Class<? extends ResultHandler<?>> resultHandler() default DefaultInitResultHandler.class;
}
