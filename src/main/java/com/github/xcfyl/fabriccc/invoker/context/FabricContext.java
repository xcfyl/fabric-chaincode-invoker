package com.github.xcfyl.fabriccc.invoker.context;

import com.github.xcfyl.fabriccc.invoker.client.FabricCaClient;
import com.github.xcfyl.fabriccc.invoker.client.FabricCryptoClient;
import com.github.xcfyl.fabriccc.invoker.config.FabricConfigProperties;
import com.github.xcfyl.fabriccc.invoker.config.OrdererConfig;
import com.github.xcfyl.fabriccc.invoker.config.PeerConfig;
import com.github.xcfyl.fabriccc.invoker.config.UserConfig;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import lombok.Data;
import org.hyperledger.fabric.sdk.*;

import java.util.*;

/**
 * @author 西城风雨楼
 */
@Data
public class FabricContext {

    /**
     * 存放当前Fabric SDK Wrapper的所有配置信息
     */
    private final FabricConfigProperties fabricConfig;

    /**
     * 存放当前的admin对象
     */
    private User admin;

    private FabricCryptoClient fabricCryptoClient;

    private FabricCaClient fabricCaClient;

    private String gopath;

    public FabricContext(FabricConfigProperties fabricConfig) {
        this.fabricConfig = fabricConfig;
        admin = loadAdmin();
        if (admin == null) {
            throw new RuntimeException("加载admin失败");
        }

        fabricCryptoClient = new FabricCryptoClient(this);
        fabricCaClient = new FabricCaClient(this);

        gopath = fabricConfig.getChainCodeConfig().getGopath();
    }

    public String getGopath() {
        return gopath;
    }

    public FabricCaClient getFabricCaClient() {
        return fabricCaClient;
    }

    public FabricCryptoClient getFabricCryptoClient() {
        return fabricCryptoClient;
    }

    /**
     * 每次调用该方法都是创建新的peer列表
     *
     * @return
     */
    public List<Peer> getPeers() {
        HFClient hfClient = CommonUtils.getHfClient(admin);
        Map<String, Peer> peerMap = loadPeerMap(hfClient);
        if (peerMap == null || peerMap.size() == 0) {
            return null;
        }
        return new ArrayList<>(peerMap.values());
    }

    public Channel getChannel(String channelName, User user) {
        HFClient hfClient = CommonUtils.getHfClient(user);
        List<Peer> peers = getPeers();
        List<Orderer> orderers = getOrderers();

        if (peers == null || orderers == null) {
            throw new RuntimeException("channel创建失败");
        }

        try {
            Channel channel = hfClient.newChannel(channelName);
            for (Orderer orderer : orderers) {
                channel.addOrderer(orderer);
            }
            for (Peer peer : peers) {
                channel.addPeer(peer);
            }

            channel.initialize();
            return channel;
        } catch (Exception e) {
            throw new RuntimeException("channel创建失败");
        }
    }

    /**
     * 每次调用该方法都是创建新的Orderer
     *
     * @return
     */
    public List<Orderer> getOrderers() {
        HFClient hfClient = CommonUtils.getHfClient(admin);
        Map<String, Orderer> orderMap = loadOrdererMap(hfClient);
        if (orderMap == null || orderMap.size() == 0) {
            return null;
        }
        return new ArrayList<>(orderMap.values());
    }

    /**
     * 加载PeerMap
     *
     * @param hfClient
     * @return
     */
    private Map<String, Peer> loadPeerMap(HFClient hfClient) {
        Map<String, Peer> map = new HashMap<>();

        try {
            for (PeerConfig peerConfig : fabricConfig.getPeerConfigs()) {
                String url = peerConfig.getUrl();
                String tlsPath = peerConfig.getTlsPath();
                String name = peerConfig.getName();
                Properties properties = CommonUtils.loadTlsProperties(tlsPath, name);
                map.put(name, hfClient.newPeer(name, url, properties));
            }
        } catch (Exception e) {
            return null;
        }
        return map;
    }

    /**
     * 加载OrdererMap
     *
     * @param hfClient
     * @return
     */
    private Map<String, Orderer> loadOrdererMap(HFClient hfClient) {
        Map<String, Orderer> map = new HashMap<>();

        try {
            for (OrdererConfig ordererConfig : fabricConfig.getOrdererConfigs()) {
                String url = ordererConfig.getUrl();
                String tlsPath = ordererConfig.getTlsPath();
                String name = ordererConfig.getName();
                Properties properties = CommonUtils.loadTlsProperties(tlsPath, name);
                map.put(name, hfClient.newOrderer(name, url, properties));
            }
        } catch (Exception e) {
            return null;
        }
        return map;
    }

    /**
     * 加载admin对象
     *
     * @return
     */
    private User loadAdmin() {
        int adminCount = 0;
        for (UserConfig userConfig : fabricConfig.getUserConfigs()) {
            if (userConfig.getIsAdmin()) {
                adminCount++;
            }
        }
        if (adminCount != 1) {
            throw new RuntimeException("存在多个admin配置");
        }

        for (UserConfig userConfig : fabricConfig.getUserConfigs()) {
            if (userConfig.getIsAdmin()) {
                admin = CommonUtils.loadUserFromLocal(
                        userConfig.getUsernameForCA(),
                        userConfig.getPasswdForCA(),
                        userConfig.getCertPath(),
                        userConfig.getPrivateKeyPath(),
                        fabricConfig.getMspConfig().getMspId());
                return admin;
            }
        }
        return null;
    }
}
