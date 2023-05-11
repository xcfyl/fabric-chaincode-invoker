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
public class ChannelConfig {
    /**
     * 通道的名称
     */
    private String channelName;

    /**
     * 当前通道关联的peer
     */
    @NestedConfigurationProperty
    private PeerConfig[] peerConfigs;

    /**
     * 当前通道关联的orderer
     */
    @NestedConfigurationProperty
    private OrdererConfig[] ordererConfigs;
}
