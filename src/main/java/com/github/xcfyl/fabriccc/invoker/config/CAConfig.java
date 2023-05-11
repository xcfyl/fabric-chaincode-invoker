package com.github.xcfyl.fabriccc.invoker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
    private String caTlsPath;

    /**
     * fabric ca的证书路径
     */
    private String caCertPath;

    private String adminName;

    private String adminPasswd;

    private String adminCertPath;

    private String adminPrivateKeyPath;

    private String adminMspId;

    private String adminAffiliation;
}
