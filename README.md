# 一、简介
该项目是对hyperledger fabric的sdk的二次封装，简化fabric智能合约的调用方式，同时提供了一些额外的工具包，借助该sdk-wrapper可以帮助尝试fabric开发的人员快速的上手fabric智能合约的java代码部署以及调用，完成简单的业务设计。
# 二、使用方式
## 3.1 配置模版
```yaml
spring:
  # 如果开启了wallet选项，那么一定要开启数据库配置
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    username: 数据库用户名
    password: 数据库密码
    url: jdbc:mysql://数据库IP:数据库端口/数据库名称?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
fabric:
  wallet:
    private-key-path: 钱包使用的私钥的地址
    public-key-path: 钱包使用的公钥的地址
    wallet-id: 钱包的唯一id，必须是整数
    type: 钱包的类型，目前仅支持db类型
  chaincode:
    ca-config:
      # CA证书路径，相对于classpath，在maven中就是resources目录
      cert-path: crypto-config/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem
      # tls的路径，如果不开启，默认为空
      tls-path:
      # CA服务器的地址
      url: http://ip地址:端口
    msp-config:
      affiliation: org1.example.com
      msp-id: Org1MSP
    chain-code-config:
      name: 部署的链码名称
      policy-path:
      project-name: 链码工程的名称
      version: 1.0
      gopath: 系统的gopath路径
    user-configs:
      - username-for-cA: admin的用户名
        passwd-for-cA: admin的密码
        is-admin: true
        cert-path: crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem
        private-key-path: crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/admin_sk
    orderer-configs:
      - name: orderer
        tls-path: crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt
        url: grpcs://ip地址:端口
    peer-configs:
      - url: grpcs://ip地址:端口
        name: peer0
        tls-path: crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/server.crt
      - url: grpcs://ip地址:端口
        name: peer1
        tls-path: crypto-config/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/server.crt

# 开启日志打印，有助于debug
logging:
  level:
    com:
      github:
        xcfyl:
          fabric:
            sdkwrapper: debug
```
## 3.2 导入方式
1. 首先将本工程克隆到本地
2. 然后使用idea打开
3. 使用idea的maven工具栏，install安装
4. 安装完毕后，即可在自己的工程里面导入对应的maven坐标即可
# 四、示例代码
本项目支持两种方式的链码调用，基于Java代码的，和基于声明式注解的，下面分别进行演示：
## 4.1 基于Java代码的调用
1. Invoke链码
```java
public class TestInvoke {
    @Test
    public void testInvokeByJava() {
        InvokeRequest<String> invokeRequest = new InvokeRequest<>(fabricContext, fabricContext.getAdmin(),
                String.class, "mychannel", "test", new String[]{"1", "2"},
                (result, e) -> {
                    // 这里编写本次调用的结果处理逻辑
                    if (e != null) {
                        // 说明发生了异常
                        throw new RuntimeException(e.getMessage());
                    } else {
                        // 可以正常处理结果
                        System.out.println(result);
                    }
                }, 1000000);
        // 这里需要说明的是，invoke是异步调用，因此他的返回值是没有意义的
        // 一般来说不需要接收该返回值
        invokeRequest.send();
    }
}
```
2. Query链码
```java
public class TestQuery {
    @Test
    public void testQueryByJava() {
        QueryRequest<String> queryRequest = new QueryRequest<>(fabricContext.getAdmin(),
                fabricContext, String.class, "mychannel", "test",
                new String[]{"1", "2"}, 1000000);
        // query是同步调用，因此query方法可以直接接收返回值
        String send = queryRequest.send();
        System.out.println(send);
    }
}
```
3. 初始化链码
```java
public class TestInit {
    @Test
    public void testInitByJava() {
        InitRequest initRequest = new InitRequest(fabricContext, "mychannel", new ResultHandler<String>() {
            @Override
            public void handle(String result, Throwable e) {
                if (e != null) {
                    throw new RuntimeException(e.getMessage());
                } else {
                    System.out.println(result);
                }
            }
        }, 10000);
        // init同invoke也是异步调用，因此不要接收返回值，该返回值始终为null
        // 处理结果的逻辑在handle方法中
        initRequest.send();
    }
}
```
4. 安装链码
```java
public class TestInstall {
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
```
## 4.2 基于声明式注解的调用
声明式注解需要遵守以下契约：
+ Invoke和Query方法的第一个接口参数必须是User对象
+ 接口的方法名和链码的函数名要保持一致，这样才知道调用的是哪一个链码函数
+ 对于Invoke方法、Init方法接口的返回值没有意义，建议始终声明为void，最终的结果处理需要借助Handler
+ 对于Install方法，接口的返回值始终为Boolean类型，返回true，代表install成功，返回false表示install失败
+ 对于Query方法，接口的返回类型就是query的结果，库会自动完成结果内容的填充

下面开始演示基于声明式注解的调用方式：

1. 启动声明式注解功能，只需要在启动类加上如下注解

```java

@SpringBootApplication
@EnableChainCodeProxy(scanPackages = "扫描路径，被ChainCodeProxy注解标注的类所在的路径")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
2. 创建接口，声明接口方法
```java
// 被该注解标注，说明当前接口需要被代理
@ChainCodeProxy(channelName = "mychannel")
public interface ChainCodeService {
    // 使用Init注解标注初始化链码的方法
    // @Init注解中包含默认的resultHandler，因此这里没有声明结果处理
    // 使用的是默认处理策略，牢记init方法的结果始终是string类型
    @Init
    void initChainCode();

    // 使用@Install注解标注安装链码的方法
    @Install
    boolean installChainCode();

    // 使用@Invoke注解标注Invoke方法，因为Invoke是异步的，因此需要在注解中
    // 指定处理结果的结果处理器，第一个参数默认必须是User，第二个参数开始就是该函数
    // 对应的链码函数的参数
    // 函数名称要和链码声明的函数保持一致
    @Invoke(resultHandler = InvokeHandler.class)
    void putUserId(User user, String userId);

    // 使用@Query注解标注query方法，query方法是同步方法，因此不需要指定
    // resultHandler，需要声明query方法的返回值，该返回值要和链码处定义的返回类型
    // 保持一致，不然会反解析错误。
    @Query(timeout = 100000)
    User queryUserById(User user, String id);
}
```
3. 调用接口方法
```java
@SpringBootTest
@Slf4j
public class TestChainCode {
    @Autowired
    private ChainCodeService chainCodeService;

    @Autowired
    private FabricContext fabricContext;

    @Test
    public void testInstallService() throws IOException {
        boolean b = chainCodeService.installChainCode();
        if (b) {
            System.out.println("安装链码成功");
        } else {
            System.out.println("安装链码失败");
        }
    }

    @Test
    public void testInitService() throws IOException {
        chainCodeService.initChainCode();
        // 这里是所以要read，因为这个调用是异步的，如果不read
        // 主线程直接执行结束，然后看不到结果
        System.in.read();
    }

    @Test
    public void testInvokeService() throws IOException {
        chainCodeService.putUserId(fabricContext.getAdmin(), "1");
        System.in.read();
    }

    @Test
    public void testQuery() throws IOException {
        System.out.println(chainCodeService.queryUserById(fabricContext.getAdmin(), "1"));
        System.in.read();
    }
}
```
# 五、钱包
该sdkwrapper中实现了基于数据库的简单钱包，所谓的钱包就是一组身份集合，如果需要使用钱包，那么需要在配置文件中声明如下配置，此外还需要在自己的数据库中，提前导入钱包的sql文件，创建钱包的表结构，sql文件在本项目的sql目录下面：

```yaml
fabric:
  wallet:
    private-key-path: 私钥路径
    public-key-path: 公钥路径
    wallet-id: 123
    type: db
```
1. private-key-path: 表示的是当前钱包使用的私钥的路径
2. public-key-path: 表示的是当前钱包使用的公钥的路径
3. wallet-id: 表示当前钱包的唯一标识，用户可以创建多个钱包，只需要保证钱包的id不一样就好了
4. type: 表示的是钱包的类型，目前仅支持db形式的钱包，需要注意的是，当前钱包的公私钥采用的均为SM2算法生成的公私钥，sdkwrapper中声明了一组SM2工具集合，可以直接使用该工具集合生成公私钥，并且private-key-path和public-key-path可以为空，如果为空，那么系统会选择一个默认的路径作为公私钥的存储位置
# 优化：
> channel应该被共享，进行池化处理