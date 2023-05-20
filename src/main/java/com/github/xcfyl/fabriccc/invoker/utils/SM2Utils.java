package com.github.xcfyl.fabriccc.invoker.utils;

import com.github.xcfyl.fabriccc.invoker.utils.sm2.Const;
import com.github.xcfyl.fabriccc.invoker.utils.sm2.SM2Client;
import com.github.xcfyl.fabriccc.invoker.utils.sm2.SM2EnginePool;
import com.github.xcfyl.fabriccc.invoker.utils.sm2.SecureRandomFactory;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static com.github.xcfyl.fabriccc.invoker.utils.sm2.SM2Client.CONVERTER;


@Slf4j
public class SM2Utils {
    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用公钥加密
     *
     * @param publicKey 公钥
     * @param message   被加密的对象
     * @return 返回加密后的字节
     */
    public static byte[] encrypt(PublicKey publicKey, String message) {
        if (message.length() == 0) {
            return new byte[0];
        }
        SM2Engine sm2Engine = null;
        byte[] encrypted = null;
        SM2EnginePool sm2EnginePool = null;
        try {
            SM2Client instance = new SM2Client();
            sm2EnginePool = new SM2EnginePool(1, SM2Engine.Mode.C1C2C3);
            sm2Engine = sm2EnginePool.borrowObject();
            encrypted = instance.encrypt(sm2Engine, publicKey, message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sm2Engine != null) {
                sm2EnginePool.returnObject(sm2Engine);
                sm2EnginePool.close();
            }
        }
        return encrypted;
    }


    /**
     * 用私钥解密
     *
     * @param privateKey 私钥
     * @param encrypted  加密的数据
     * @return 返回被解密的对象
     */
    public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) {
        if (encrypted.length == 0) {
            return new byte[0];
        }

        SM2EnginePool sm2EnginePool = new SM2EnginePool(1, SM2Engine.Mode.C1C2C3);
        SM2Engine sm2Engine = null;
        byte[] rs = null;
        try {
            SM2Client instance = new SM2Client();
            sm2Engine = sm2EnginePool.borrowObject();
            rs = instance.decrypt(sm2Engine, privateKey, encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sm2Engine != null) {
                sm2EnginePool.returnObject(sm2Engine);
                sm2EnginePool.close();
            }
        }

        return rs;
    }

    /**
     * 生成公私钥对
     *
     * @return 生成成功，返回true，否则返回false
     */
    public static KeyPair generateKeyPair() {
        try {
            SM2Client sm2Util = new SM2Client();
            return sm2Util.generatekeyPair();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("生成密钥对失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 返回私钥的字符串
     *
     * @param keyPair 密钥对
     * @return 返回私钥字符串
     */
    public static String getPrivateKeyStrFromKeyPair(KeyPair keyPair) {
        try {
            return SM2Client.pemFrom(keyPair.getPrivate(), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回公钥字符串
     *
     * @param keyPair 密钥对
     * @return 返回公钥字符串
     */
    public static String getPublicKeyStrFromKeyPair(KeyPair keyPair) {
        try {
            return SM2Client.pemFrom(keyPair.getPublic());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过私钥对消息进行签名
     *
     * @param privateKey
     * @param message
     * @return
     */
    public static String sign(PrivateKey privateKey, String message) {
        synchronized (SM2Utils.class) {
            try {
                Signature signature = Signature.getInstance(Const.SM3SM2_VALUE, BouncyCastleProvider.PROVIDER_NAME);
                signature.initSign(privateKey, SecureRandomFactory.getSecureRandom());
                signature.update(message.getBytes());
                byte[] sign = signature.sign();
                return new String(sign);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 通过公钥进行验证
     *
     * @param publicKey
     * @param message
     * @param sigBytes
     * @return
     */
    public static boolean verify(PublicKey publicKey, byte[] message, byte[] sigBytes) {
        synchronized (SM2Utils.class) {
            try {
                Signature signature = Signature.getInstance(Const.SM3SM2_VALUE, BouncyCastleProvider.PROVIDER_NAME);
                signature.initVerify(publicKey);
                signature.update(message);
                return signature.verify(sigBytes);
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static void savePemFormatKeyFile(PrivateKey privateKey, String filename) throws IOException, OperatorCreationException {
        String privateKeyPem = SM2Client.pemFrom(privateKey, "");
        Files.write(Paths.get(filename), privateKeyPem.getBytes());
    }

    public static void savePemFormatPubKeyFile(PublicKey publicKey, String filename) throws IOException {
        String pubKeyPem = SM2Client.pemFrom(publicKey);
        Files.write(Paths.get(filename), pubKeyPem.getBytes());
    }

    public static void saveKeyPairInPem(KeyPair keyPair, String pubFileName, String privFileName) throws IOException, OperatorCreationException {
        savePemFormatKeyFile(keyPair.getPrivate(), privFileName);
        savePemFormatPubKeyFile(keyPair.getPublic(), pubFileName);
    }

    public static PrivateKey loadPrivateKeyFromFile(Reader reader, String password) throws IOException, OperatorCreationException, PKCSException {
        PrivateKey priv = null;
        try (PEMParser pemParser = new PEMParser(reader)) {
            Object obj = pemParser.readObject();
            if (password != null && password.length() > 0) {
                if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
                    PKCS8EncryptedPrivateKeyInfo epkInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
                    InputDecryptorProvider decryptProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .build(password.toCharArray());
                    PrivateKeyInfo pkInfo = epkInfo.decryptPrivateKeyInfo(decryptProvider);
                    priv = CONVERTER.getPrivateKey(pkInfo);
                }
            } else {
                priv = CONVERTER.getPrivateKey((PrivateKeyInfo) obj);
            }
        }
        return priv;
    }

    public static PublicKey loadPublicKeyFromFile(Reader reader) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (PemReader pemReader = new PemReader(reader)) {
            PemObject pemObject = pemReader.readPemObject();
            Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
            return KeyFactory.getInstance(Const.EC_VALUE, BouncyCastleProvider.PROVIDER_NAME).generatePublic(new X509EncodedKeySpec(pemObject.getContent()));
        }
    }
}
