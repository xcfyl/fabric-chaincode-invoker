package com.github.xcfyl.fabriccc.invoker.service;

import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.request.ResultHandler;
import com.github.xcfyl.fabriccc.invoker.request.InitRequest;
import com.github.xcfyl.fabriccc.invoker.request.InstallRequest;
import com.github.xcfyl.fabriccc.invoker.request.InvokeRequest;
import com.github.xcfyl.fabriccc.invoker.request.QueryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @author 西城风雨楼
 */
@SpringBootTest
public class TestService {
    @Autowired
    private InvokeService invokeService;

    @Autowired
    private QueryService queryService;

    @Autowired
    FabricContext fabricContext;

    @Test
    public void testInvoke() throws IOException {
        invokeService.person(fabricContext.getAdmin(), "zhangsan", "22");
        // 这里是为了等待主调用不要结束
        int ignored = System.in.read();
    }

    @Test
    public void testQuery() {
        Person person = queryService.person(fabricContext.getAdmin(), "lisi", "10");
        System.out.println(person);
    }

    @Test
    public void testInvokeByJava() {
        InvokeRequest<String> invokeRequest = new InvokeRequest<>(fabricContext, fabricContext.getAdmin(),
                String.class, String.class, "mychannel", "test", new String[]{"1", "2"},
                new ResultHandler<String>() {
                    @Override
                    public void handleSuccess(String result) {

                    }

                    @Override
                    public void handleFailure(Throwable throwable) {

                    }
                }, 1000000);
        // 这里需要说明的是，invoke是异步调用，因此他的返回值是没有意义的
        // 一般来说不需要接收该返回值
        invokeRequest.send();
    }

    @Test
    public void testQueryByJava() {
        QueryRequest<String> queryRequest = new QueryRequest<>(fabricContext.getAdmin(),
                fabricContext, String.class, String.class, "mychannel", "test",
                new String[]{"1", "2"}, 1000000);
        // query是同步调用，因此query方法可以直接接收返回值
        String send = queryRequest.send();
        System.out.println(send);
    }

    @Test
    public void testInitByJava() {
        InitRequest initRequest = new InitRequest(fabricContext, "mychannel", new ResultHandler<String>() {
            @Override
            public void handleSuccess(String result) {

            }

            @Override
            public void handleFailure(Throwable throwable) {

            }
        }, 10000);
        // init同invoke也是异步调用，因此不要接收返回值，该返回值始终为null
        // 处理结果的逻辑在handle方法中
        initRequest.send();
    }

    @Test
    public void testInstallByJava() {
        InstallRequest installRequest = new InstallRequest(fabricContext, "mychannel", 10000);
        // install是同步调用，因此可以直接接收结果
        // install调用的结果始终是bool类型
        Boolean send = installRequest.send();
        if (send) {
            System.out.println("安装成功");
        } else {
            System.out.println("安装失败");
        }
    }
}
