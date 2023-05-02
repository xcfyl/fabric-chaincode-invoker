package com.github.xcfyl.fabriccc.invoker.utils;

import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.config.ChainCodeConfig;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.*;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author 西城风雨楼
 */
public class CommonUtils {
    public static Channel getChannel(String channelName, FabricContext context) {
        HFClient hfClient = context.getHfClientOfAdmin();
        List<Peer> peers = context.getPeers();
        List<Orderer> orderers = context.getOrderers();

        if (peers == null || orderers == null || hfClient == null) {
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
     * 从本地文件中加载admin对象
     *
     * @param username 用户名
     * @param password 密码
     * @param certPath 公钥的文件
     * @param pkPath 私钥文件
     * @param mspId mspId
     * @return 返回admin对象
     */
    public static User loadAdminFromLocal(String username,
                                   String password,
                                   String certPath,
                                   String pkPath,
                                   String mspId) {
        PrivateKey key;
        String certificate;
        BufferedInputStream keyReader = null;
        try {
            // 找到key-store所在目录文件
            InputStream pkResource = CommonUtils.class.getClassLoader().getResourceAsStream(pkPath);
            if (pkResource == null) {
                throw new RuntimeException("私钥文件加载失败");
            }
            keyReader = new BufferedInputStream(pkResource);
            byte[] byteArray = IOUtils.toByteArray(keyReader);
            key = FabricCryptoUtils.getPrivateKey(byteArray);
            InputStream certResource = CommonUtils.class.getClassLoader().getResourceAsStream(certPath);
            if (certResource == null) {
                throw new RuntimeException("cert文件解析失败");
            }
            byte[] bytes = IOUtils.toByteArray(certResource);
            certificate = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("证书解析失败");
        } finally {
            try {
                if (keyReader != null) {
                    keyReader.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        Enrollment enrollment = new X509Enrollment(key, certificate);
        FabricUser fabricUser = new FabricUser();
        fabricUser.setEnrollment(enrollment);
        fabricUser.setName(username);
        fabricUser.setPassword(password);
        fabricUser.setMspId(mspId);
        return fabricUser;
    }

    public static ChaincodeID getChainCodeId(FabricContext context) {
        ChaincodeID.Builder builder = ChaincodeID.newBuilder();
        ChainCodeConfig chainCodeConfig = context.getFabricConfig().getChainCodeConfig();
        builder.setName(chainCodeConfig.getName());
        builder.setPath(chainCodeConfig.getProjectName());
        builder.setVersion(chainCodeConfig.getVersion());
        return builder.build();
    }

    public static HFClient getHfClient(User user) {
        try {
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            HFClient hfClient = HFClient.createNewInstance();
            hfClient.setCryptoSuite(cryptoSuite);
            hfClient.setUserContext(user);
            return hfClient;
        } catch (Exception e) {
            throw new RuntimeException("获取HClient对象失败");
        }
    }

    public static boolean allResponsesIsOk(Collection<ProposalResponse> responses) {
        boolean success = true;
        for (ProposalResponse response : responses) {
            if (response.getStatus() == ChaincodeResponse.Status.FAILURE) {
                success = false;
                break;
            }
        }
        return success;
    }

    public static byte[] parseResponse(Collection<ProposalResponse> responses) throws InvalidArgumentException {
        for (ProposalResponse response : responses) {
            return response.getChaincodeActionResponsePayload();
        }
        return new byte[0];
    }

    /**
     * 从指定的文件中加载tls文件，并将其加载为一个properties格式
     *
     * @param tlsPath          文件路径
     * @param hostnameOverride properties中的一个属性
     * @return 如果加载成功，返回properties对象，否则返回false
     */
    public static Properties loadTlsProperties(String tlsPath, String hostnameOverride) {
        Properties properties = new Properties();
        String certContent;
        try {
            InputStream tlsResource = CommonUtils.class.getClassLoader().getResourceAsStream(tlsPath);
            if (tlsResource == null) {
                throw new RuntimeException("tls文件加载失败");
            }
            certContent = new String(IOUtils.toByteArray(tlsResource));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        properties.put("pemBytes", certContent.getBytes());
        properties.setProperty("sslProvider", "openSSL");
        properties.setProperty("negotiationType", "TLS");
        properties.setProperty("trustServerCertificate", "true");
        properties.setProperty("hostnameOverride", hostnameOverride);
        return properties;
    }
}
