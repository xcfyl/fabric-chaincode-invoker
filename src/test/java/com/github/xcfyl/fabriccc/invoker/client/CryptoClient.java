package com.github.xcfyl.fabriccc.invoker.client;

import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

/**
 * @author 西城风雨楼
 */
@SpringBootTest
public class CryptoClient {
    @Autowired
    private FabricCryptoClient fabricCryptoClient;

    @Autowired
    private FabricContext fabricContext;

    @Test
    public void testHash() {
        byte[] hash = fabricCryptoClient.hash(fabricContext.getAdmin().getEnrollment().getCert().getBytes());
        System.out.println(hash.length);
        byte[] encode = Base64.getEncoder().encode(hash);
        System.out.println(encode.length);
    }
}
