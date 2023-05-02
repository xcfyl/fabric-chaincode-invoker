package com.github.xcfyl.fabriccc.invoker.annotation;

import com.github.xcfyl.fabriccc.invoker.processor.ChainCodeProxyImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否开启链码代理
 *
 * @author 西城风雨楼
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ChainCodeProxyImportSelector.class)
public @interface EnableChainCodeProxy {
    // 扫描的包路径
    String[] scanPackages();
}
