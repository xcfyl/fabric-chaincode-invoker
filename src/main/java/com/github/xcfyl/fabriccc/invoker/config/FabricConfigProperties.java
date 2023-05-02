package com.github.xcfyl.fabriccc.invoker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author 西城风雨楼
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("fabric.chaincode")
public class FabricConfigProperties {
    /**
     * CA相关的配置信息
     */
    @NestedConfigurationProperty
    private CAConfig caConfig;

    /**
     * 链码相关的配置信息
     */
    @NestedConfigurationProperty
    private ChainCodeConfig chainCodeConfig;

    /**
     * msp相关的配置信息
     */
    @NestedConfigurationProperty
    private MSPConfig mspConfig;

    /**
     * 排序节点相关的信息
     */
    @NestedConfigurationProperty
    private OrdererConfig[] ordererConfigs;

    /**
     * peer节点相关的信息
     */
    @NestedConfigurationProperty
    private PeerConfig[] peerConfigs;

    /**
     * 用户相关的信息
     */
    @NestedConfigurationProperty
    private UserConfig[] userConfigs;
}
