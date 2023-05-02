package com.github.xcfyl.fabriccc.invoker.context;

import cn.hutool.core.util.StrUtil;
import com.github.xcfyl.fabriccc.invoker.client.FabricCaClient;
import com.github.xcfyl.fabriccc.invoker.client.FabricCryptoClient;
import com.github.xcfyl.fabriccc.invoker.config.FabricConfigProperties;
import com.github.xcfyl.fabriccc.invoker.config.OrdererConfig;
import com.github.xcfyl.fabriccc.invoker.config.PeerConfig;
import com.github.xcfyl.fabriccc.invoker.config.UserConfig;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import lombok.Data;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.*;

/**
 * @author 西城风雨楼
 */
@Data
public class FabricContext {
    /**
     * 存放peerName和peer对象的映射
     */
    private final Map<String, Peer> peerMap;

    /**
     * 存放ordererName和orderer对象的映射
     */
    private final Map<String, Orderer> ordererMap;

    /**
     * 存放当前Fabric SDK Wrapper的所有配置信息
     */
    private final FabricConfigProperties fabricConfig;

    /**
     * 存放当前的admin对象
     */
    private User admin;

    /**
     * 存放当前admin对象创建好的hfClient对象
     */
    private HFClient hfClientOfAdmin;

    private HFCAClient hfCAClient;

    private FabricCryptoClient fabricCryptoClient;

    private FabricCaClient fabricCaClient;

    private String gopath;

    public FabricContext(FabricConfigProperties fabricConfig) {
        this.fabricConfig = fabricConfig;
        admin = loadAdmin();
        if (admin == null) {
            throw new RuntimeException("加载admin失败");
        }

        hfClientOfAdmin = CommonUtils.getHfClient(admin);

        hfCAClient = loadHfCaClient();

        peerMap = loadPeerMap(hfClientOfAdmin);
        if (peerMap == null) {
            throw new RuntimeException("加载peer对象失败");
        }

        ordererMap = loadOrdererMap(hfClientOfAdmin);
        if (ordererMap == null) {
            throw new RuntimeException("加载orderer对象失败");
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

    private HFCAClient loadHfCaClient() {
        CryptoSuite cryptoSuite;
        try {
            cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        } catch (Exception e) {
            return null;
        }

        String url = fabricConfig.getCaConfig().getUrl();
        if (StrUtil.isBlank(url)) {
            return null;
        }

        HFCAClient hfcaClient;
        try {
            hfcaClient = HFCAClient.createNewInstance(url, null);
        } catch (Exception e) {
            return null;
        }
        hfcaClient.setCryptoSuite(cryptoSuite);
        return hfcaClient;
    }

    public List<Peer> getPeers() {
        return new ArrayList<>(peerMap.values());
    }

    public List<Orderer> getOrderers() {
        return new ArrayList<>(ordererMap.values());
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
                admin = CommonUtils.loadAdminFromLocal(
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
