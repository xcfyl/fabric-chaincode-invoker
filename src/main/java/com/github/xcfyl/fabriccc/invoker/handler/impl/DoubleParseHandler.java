package com.github.xcfyl.fabriccc.invoker.handler.impl;

import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

/**
 * @author 西城风雨楼
 */
public class DoubleParseHandler implements TypeParseHandler<Double> {

    @Override
    public Double parse(byte[] data) {
        return Double.valueOf(new String(data));
    }

    @Override
    public String get(Double obj) {
        return Double.toString(obj);
    }
}
