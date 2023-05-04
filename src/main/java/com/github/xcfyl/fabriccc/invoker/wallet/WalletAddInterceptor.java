package com.github.xcfyl.fabriccc.invoker.wallet;

import java.security.KeyPair;

/**
 * @author 西城风雨楼
 */
public interface WalletAddInterceptor {
    /**
     * 添加之前会调用该逻辑
     *
     * @param walletInfo 钱包信息，这些钱包信息最终会被保存在钱包中，用户可以在保存之前修改
     * @return 如果preAdd返回null，那么就继续执行后面的逻辑，否则直接返回preAdd返回的结果
     */
    Boolean preAdd(KeyPair keyPair, WalletInfo walletInfo);

    /**
     * 添加之后会调用的逻辑
     *
     * @param walletInfo 钱包信息
     * @return 如果afterAdd返回null，那么就继续执行后面的逻辑，否则直接返回afterAdd的逻辑
     */
    Boolean afterAdd(KeyPair keyPair, WalletInfo walletInfo);
}
