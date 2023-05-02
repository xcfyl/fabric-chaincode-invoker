package com.github.xcfyl.fabriccc.invoker.handler;

/**
 * @author 西城风雨楼
 */
public interface TypeParseHandler<T> {
    T parse(byte[] data);

    /**
     * 将当前类型转换为String的形式
     *
     * @param obj
     * @return
     */
    String get(T obj);
}
