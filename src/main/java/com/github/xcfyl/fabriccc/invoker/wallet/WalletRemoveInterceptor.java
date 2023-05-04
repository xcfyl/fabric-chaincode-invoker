package com.github.xcfyl.fabriccc.invoker.wallet;

import java.security.KeyPair;
import java.util.List;

/**
 * @author 西城风雨楼
 */
public interface WalletRemoveInterceptor {
    /**
     * 移除之前的前置逻辑
     *
     * @param walletInfoList 移除的用户在钱包中的所有信息
     * @param force
     * @return
     */
    Boolean preRemove(KeyPair keyPair, List<WalletInfo> walletInfoList, boolean force);

    /**
     * 移除之后的逻辑
     *
     * @param walletInfoList 移除的用户在钱包中的所有信息
     * @param force
     * @return
     */
    Boolean afterRemove(KeyPair keyPair, List<WalletInfo> walletInfoList, boolean force);
}
