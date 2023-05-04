package com.github.xcfyl.fabriccc.invoker.handler.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

import java.text.SimpleDateFormat;

/**
 * @author 西城风雨楼
 */
public class JsonParseHandler<T> implements TypeParseHandler<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();


    private final Class<T> clazz;

    public JsonParseHandler(Class<T> clazz) {
        this.clazz = clazz;
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public T parse(byte[] data) {
        return JSONUtil.toBean(new String(data), clazz);
    }

    @Override
    public String get(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
