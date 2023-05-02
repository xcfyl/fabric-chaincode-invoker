package com.github.xcfyl.fabriccc.invoker.handler.resparseimpl;

import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

/**
 * @author 西城风雨楼
 */
public class BooleanParseHandler implements TypeParseHandler<Boolean> {

    @Override
    public Boolean parse(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        return Boolean.valueOf(new String(data));
    }

    @Override
    public String get(Boolean obj) {
        return obj.toString();
    }
}
