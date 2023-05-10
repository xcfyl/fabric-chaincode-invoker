package com.github.xcfyl.fabriccc.invoker.handler.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author 西城风雨楼
 */
public class JsonParseHandler<T> implements TypeParseHandler<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();


    private final Class<T> clazz;

    private final Class<?> genericClazz;


    public JsonParseHandler(Class<T> clazz, Class<?> genericClazz) {
        this.clazz = clazz;
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        this.genericClazz = genericClazz;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T parse(byte[] data) {
        if (clazz.isAssignableFrom(List.class)) {
            if (genericClazz == null) {
                throw new RuntimeException("没有指定泛型类型");
            }
            return (T) JSONUtil.toList(new String(data), genericClazz);
        }
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
