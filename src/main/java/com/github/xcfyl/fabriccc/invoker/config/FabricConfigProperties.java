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
@ConfigurationProperties("fabric.context")
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
     * 通道配置
     */
    @NestedConfigurationProperty
    private ChannelConfig channelConfig;
}
