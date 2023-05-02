package com.github.xcfyl.fabriccc.invoker.other;

import com.github.xcfyl.fabriccc.invoker.client.FabricCryptoClient;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import org.hyperledger.fabric.sdk.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author 西城风雨楼
 */
@SpringBootTest
public class TestCrypto {
    @Autowired
    private FabricCryptoClient cryptoClient;

    @Autowired
    private FabricContext fabricContext;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        User admin = fabricContext.getAdmin();
        byte[] sign = cryptoClient.sign(admin, "123".getBytes());
        System.out.println(cryptoClient.verify(admin, sign, "123".getBytes()));
    }

    @Test
    public void testResource() throws IOException {
        Resource resource = applicationContext.getResource("classpath:.");
        System.out.println(resource.getURI().getPath());
    }
}
