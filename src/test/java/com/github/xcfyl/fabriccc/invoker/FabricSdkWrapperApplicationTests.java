package com.github.xcfyl.fabriccc.invoker;

import com.github.xcfyl.fabriccc.invoker.annotation.EnableChainCodeProxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@SpringBootApplication
@Slf4j
@EnableChainCodeProxy(scanPackages = "com.github.xcfyl.fabric.sdkwrapper")
class FabricSdkWrapperApplicationTests {

    @Test
    void contextLoads() {
    }

}
