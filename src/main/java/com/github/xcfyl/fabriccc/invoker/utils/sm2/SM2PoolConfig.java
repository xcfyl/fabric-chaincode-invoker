package com.github.xcfyl.fabriccc.invoker.utils.sm2;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.Map;

public class SM2PoolConfig extends GenericObjectPoolConfig {

    public SM2PoolConfig() {
    }

    public SM2PoolConfig(String file) throws IOException {
        this.setProperties(ConfigLoader.loadConfig(this.getClass().getResourceAsStream(file), Const.SM2));
    }

    private void setProperties(Map<String, Object> map) {
        this.setMaxTotal(Integer.valueOf(map.get("maxTotal").toString()));
        this.setMaxIdle(Integer.valueOf(map.get("maxIdle").toString()));
        this.setMinIdle(Integer.valueOf(map.get("minIdle").toString()));
        this.setMaxWaitMillis(Integer.valueOf(map.get("maxWaitMillis").toString()));
    }
}