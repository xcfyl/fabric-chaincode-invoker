package com.github.xcfyl.fabriccc.invoker.wallet;

import java.security.KeyPair;
import java.util.List;

/**
 * @author 西城风雨楼
 */
public interface WalletQueryInterceptor {
    /**
     * 对查询的结果进行过滤
     *
     * @param keyPair 钱包的公私钥对
     * @param walletInfos 查询出来的钱包信息
     * @return 用户可以在该拦截器中修改walletInfos，返回一个新的walletInfo列表
     */
    List<WalletInfo> filterWalletInfo(KeyPair keyPair, List<WalletInfo> walletInfos);
}
