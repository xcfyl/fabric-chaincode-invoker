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
public class CAConfig {
    /**
     * fabric ca的url路径
     */
    private String url;

    /**
     * fabric ca的tls路径
     */
    private String tlsPath;

    /**
     * fabric ca的证书路径
     */
    private String certPath;
}
