package com.github.xcfyl.fabriccc.invoker.context;

import cn.hutool.core.util.StrUtil;
import com.github.xcfyl.fabriccc.invoker.config.CAConfig;
import com.github.xcfyl.fabriccc.invoker.config.FabricConfigProperties;
import com.github.xcfyl.fabriccc.invoker.config.OrdererConfig;
import com.github.xcfyl.fabriccc.invoker.config.PeerConfig;
import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author 西城风雨楼
 */
public class FabricContext {
    /**
     * 存放配置
     */
    private final FabricConfigProperties config;

    private Channel channel;

    private HFClient hfClient;

    private HFCAClient hfcaClient;

    private final List<Peer> peers = new ArrayList<>();

    private final List<Orderer> orderers = new ArrayList<>();

    private User admin;

    private CryptoPrimitives cryptoPrimitives;

    public FabricContext(FabricConfigProperties properties) {
        this.config = properties;
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException("fabric context初始化失败");
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public List<Orderer> getOrderers() {
        return orderers;
    }

    public User getAdmin() {
        return admin;
    }

    public FabricConfigProperties getConfig() {
        return config;
    }

    /**
     * 让用户user对数据data进行签名
     *
     * @param user 用户名
     * @param data 数据
     * @return 返回字节数组
     */
    public byte[] sign(User user, byte[] data) {
        try {
            return cryptoPrimitives.sign(user.getEnrollment().getKey(), data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对数据进行Hash
     *
     * @param data
     * @return
     */
    public byte[] hash(byte[] data) {
        return cryptoPrimitives.hash(data);
    }

    public boolean verify(User user, byte[] signature, byte[] plainText) {
        try {
            byte[] certBytes = user.getEnrollment().getCert().getBytes();
            return cryptoPrimitives.verify(certBytes, "SHA256withECDSA", signature, plainText);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向CA注册用户
     */
    public boolean registerUser(String username, String password) {
        try {
            if (hfcaClient == null) {
                return false;
            }
            RegistrationRequest registrationRequest = new RegistrationRequest(username);
            registrationRequest.setSecret(password);
            String affiliation = admin.getAffiliation();
            registrationRequest.setAffiliation(affiliation);
            String secret = hfcaClient.register(registrationRequest, admin);
            return !StrUtil.isBlank(secret);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 撤销某个用户的证书
     */
    public boolean revokeUser(String revokedUser) {
        try {
            if (hfcaClient == null) {
                return false;
            }
            hfcaClient.revoke(admin, revokedUser, "revoked");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 注册的同时，获取该用户的证书
     */
    public User registerAndEnroll(String username, String passwd) {
        if (registerUser(username, passwd)) {
            return enrollUser(username, passwd);
        }
        return null;
    }

    /**
     * 获取某个用户的证书，前提是该用户已经注册过了
     */
    public User enrollUser(String username, String password) {
        if (hfcaClient == null) {
            return null;
        }
        Enrollment enrollment;
        try {
            enrollment = hfcaClient.enroll(username, password);
        } catch (Exception e) {
            return null;
        }
        FabricUser fabricUser = new FabricUser();
        fabricUser.setName(username);
        fabricUser.setPassword(password);
        fabricUser.setMspId(admin.getMspId());
        fabricUser.setEnrollment(enrollment);

        return fabricUser;
    }

    private void init() throws Exception {
        // 初始化admin
        admin = getAdminUser();
        // 初始化hf客户端
        hfClient = getHfClient(admin);
        // 初始化ca套件
        hfcaClient = getHfcaClient();
        channel = getChanel();
        cryptoPrimitives = getCryptoPrimitives();
    }

    private CryptoPrimitives getCryptoPrimitives() {
        BufferedInputStream bis = null;
        CryptoPrimitives cryptoPrimitives;
        try {
            cryptoPrimitives = new CryptoPrimitives();
            cryptoPrimitives.init();
            String certPath = config.getCaConfig().getCaCertPath();
            InputStream stream = FabricContext.class.getClassLoader().getResourceAsStream(certPath);
            if (stream == null) {
                throw new RuntimeException("ca证书路径没有配置");
            }

            bis = new BufferedInputStream(stream);
            cryptoPrimitives.addCACertificatesToTrustStore(bis);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("CryptoClient创建失败");
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cryptoPrimitives;
    }

    private HFClient getHfClient(User admin) {
        return CommonUtils.getHfClient(admin);
    }

    private HFCAClient getHfcaClient() throws Exception {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        String url = config.getCaConfig().getUrl();
        HFCAClient hfcaClient = HFCAClient.createNewInstance(url, null);
        hfcaClient.setCryptoSuite(cryptoSuite);
        return hfcaClient;
    }

    private User getAdminUser() {
        CAConfig caConfig = config.getCaConfig();
        return CommonUtils.loadUserFromLocal(caConfig.getAdminName(),
                caConfig.getAdminPasswd(), caConfig.getAdminCertPath(),
                caConfig.getAdminPrivateKeyPath(), caConfig.getAdminMspId());
    }


    private Channel getChanel() throws Exception {
        String channelName = config.getChannelConfig().getChannelName();
        channel = hfClient.newChannel(channelName);

        // 创建当前channel的peer
        for (PeerConfig peerConfig : config.getChannelConfig().getPeerConfigs()) {
            String url = peerConfig.getUrl();
            String tlsPath = peerConfig.getTlsPath();
            String name = peerConfig.getName();
            Properties properties = CommonUtils.loadTlsProperties(tlsPath, name);
            Peer peer = hfClient.newPeer(name, url, properties);
            peers.add(peer);
            channel.addPeer(peer);
        }

        // 创建当前channel的orderer
        for (OrdererConfig ordererConfig : config.getChannelConfig().getOrdererConfigs()) {
            String url = ordererConfig.getUrl();
            String tlsPath = ordererConfig.getTlsPath();
            String name = ordererConfig.getName();
            Properties properties = CommonUtils.loadTlsProperties(tlsPath, name);
            Orderer orderer = hfClient.newOrderer(name, url, properties);
            orderers.add(orderer);
            channel.addOrderer(orderer);
        }
        channel.initialize();
        return channel;
    }
}
