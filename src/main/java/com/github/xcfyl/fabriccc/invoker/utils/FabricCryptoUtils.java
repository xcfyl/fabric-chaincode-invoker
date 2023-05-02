package com.github.xcfyl.fabriccc.invoker.utils;

import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author 西城风雨楼
 */
public class FabricCryptoUtils {
    public static PrivateKey getPrivateKey(byte[] permKey) {
        try {
            StringBuilder keyBuilder = new StringBuilder();
            StringReader stringReader = new StringReader(new String(permKey));
            BufferedReader keyReader = new BufferedReader(stringReader);
            for (String line = keyReader.readLine(); line != null; line = keyReader.readLine()) {
                if (!line.contains("PRIVATE")) {
                    keyBuilder.append(line);
                }
            }
            byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前用户的X509证书对象
     *
     * @param user
     * @return
     */
    public static X509Certificate getX509Certificate(User user) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(
                    new ByteArrayInputStream(user.getEnrollment().
                            getCert().getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getPrivateKeyBytes(User user) {
        Enrollment enrollment = user.getEnrollment();
        PrivateKey key = enrollment.getKey();
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
            PKCS8Generator generator = new JcaPKCS8Generator(key, null);
            pemWriter.writeObject(generator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.getBuffer().toString().getBytes();
    }
}
