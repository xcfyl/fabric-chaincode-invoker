package com.github.xcfyl.fabriccc.invoker.config;

import com.github.xcfyl.fabriccc.invoker.wallet.WalletAddInterceptor;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletInfo;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletRemoveInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author 西城风雨楼
 */
@Configuration
public class InterceptorConfig {
    @Bean
    public WalletAddInterceptor addInterceptor() {
        return new WalletAddInterceptor() {
            @Override
            public Boolean preAdd(WalletInfo walletInfo) {
                System.out.println("添加前置方法");
                System.out.println(walletInfo);
                return null;
            }

            @Override
            public Boolean afterAdd(WalletInfo walletInfo) {
                System.out.println("添加后置方法");
                System.out.println(walletInfo);
                return null;
            }
        };
    }

    @Bean
    public WalletRemoveInterceptor removeInterceptor() {
        return new WalletRemoveInterceptor() {
            @Override
            public Boolean preRemove(List<WalletInfo> walletInfoList, boolean force) {
                System.out.println("删除前置方法");
                System.out.println(walletInfoList);
                return null;
            }

            @Override
            public Boolean afterRemove(List<WalletInfo> walletInfoList, boolean force) {
                System.out.println("删除后置方法");
                System.out.println(walletInfoList);
                return null;
            }
        };
    }
}
