package com.github.xcfyl.fabriccc.invoker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 钱包的配置
 *
 * @author 西城风雨楼
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("fabric.wallet")
public class DbWalletConfigProperties {
    /**
     * 当前钱包的id
     */
    private Integer walletId;

    /**
     * 钱包的类型
     */
    private String type;

    /**
     * 钱包的公钥
     */
    private String publicKeyPath;

    /**
     * 钱包的私钥
     */
    private String privateKeyPath;
}

