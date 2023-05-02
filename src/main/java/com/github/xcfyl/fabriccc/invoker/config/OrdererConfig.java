package com.github.xcfyl.fabriccc.invoker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 西城风雨楼
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdererConfig {
    /**
     * peer结点的名称
     */
    private String name;

    /**
     * tls证书路径
     */
    private String tlsPath;

    /**
     * ip地址+端口号
     */
    private String url;
}
