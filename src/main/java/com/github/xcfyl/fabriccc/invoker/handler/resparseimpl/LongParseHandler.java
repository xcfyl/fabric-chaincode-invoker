package com.github.xcfyl.fabriccc.invoker.handler.resparseimpl;

import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

/**
 * @author 西城风雨楼
 */
public class LongParseHandler implements TypeParseHandler<Long> {

    @Override
    public Long parse(byte[] data) {
        return Long.valueOf(new String(data));
    }

    @Override
    public String get(Long obj) {
        return Long.toString(obj);
    }
}
