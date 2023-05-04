package com.github.xcfyl.fabriccc.invoker.wallet;

import java.security.KeyPair;
import java.util.List;

/**
 * @author 西城风雨楼
 */
public interface WalletQueryInterceptor {
    List<WalletInfo> filterWalletInfo(KeyPair keyPair, List<WalletInfo> walletInfos);
}
