package com.github.xcfyl.fabriccc.invoker.handler.impl;

import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

/**
 * @author 西城风雨楼
 */
public class FloatParseHandler implements TypeParseHandler<Float> {

    @Override
    public Float parse(byte[] data) {
        return Float.valueOf(new String(data));
    }

    @Override
    public String get(Float obj) {
        return Float.toString(obj);
    }
}
