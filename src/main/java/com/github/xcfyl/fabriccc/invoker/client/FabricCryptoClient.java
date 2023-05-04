package com.github.xcfyl.fabriccc.invoker.client;

import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import com.github.xcfyl.fabriccc.invoker.utils.SM2Utils;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * 提供加解密相关的，比如使用当前用户证书进行签名和验签名等功能
 *
 * @author 西城风雨楼
 */
public class FabricCryptoClient {
    private final CryptoPrimitives cryptoPrimitives;

    public FabricCryptoClient(FabricContext fabricContext) {
        BufferedInputStream bis = null;
        try {
            cryptoPrimitives = new CryptoPrimitives();
            cryptoPrimitives.init();
            String certPath = fabricContext.getFabricConfig().getCaConfig().getCertPath();
            InputStream stream = FabricCryptoClient.class.getClassLoader().getResourceAsStream(certPath);
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
}
