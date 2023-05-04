package com.github.xcfyl.fabriccc.invoker.handler.impl;

import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

/**
 * @author 西城风雨楼
 */
public class IntegerParseHandler implements TypeParseHandler<Integer> {

    @Override
    public Integer parse(byte[] data) {
        return Integer.valueOf(new String(data));
    }

    @Override
    public String get(Integer obj) {
        return Integer.toString(obj);
    }
}
