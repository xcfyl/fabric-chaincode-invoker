package com.github.xcfyl.fabriccc.invoker.handler.resparseimpl;

import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

/**
 * @author 西城风雨楼
 */
public class StringParseHandler implements TypeParseHandler<String> {

    @Override
    public String parse(byte[] data) {
        return new String(data);
    }

    @Override
    public String get(String obj) {
        return obj;
    }
}
