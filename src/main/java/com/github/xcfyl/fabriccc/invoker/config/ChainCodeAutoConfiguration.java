package com.github.xcfyl.fabriccc.invoker.config;

import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletQueryInterceptor;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletRemoveInterceptor;
import com.github.xcfyl.fabriccc.invoker.wallet.impl.FabricDbWallet;
import com.github.xcfyl.fabriccc.invoker.condition.ContainsDbTypeWalletCondition;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletAddInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author 西城风雨楼
 */
@Configuration
@EnableConfigurationProperties({FabricConfigProperties.class, DbWalletConfigProperties.class})
public class ChainCodeAutoConfiguration {

    @Bean
    public FabricContext fabricContext(FabricConfigProperties properties) {
        return new FabricContext(properties);
    }

    @Bean
    @Conditional(ContainsDbTypeWalletCondition.class)
    @ConditionalOnMissingBean(FabricDbWallet.class)
    public FabricDbWallet fabricDbWallet(FabricContext context, JdbcTemplate jdbcTemplate,
                                         DbWalletConfigProperties properties,
                                         ApplicationContext applicationContext) {
        ObjectProvider<WalletRemoveInterceptor> removeInterceptorObjectProvider = applicationContext.getBeanProvider(WalletRemoveInterceptor.class);
        ObjectProvider<WalletAddInterceptor> addInterceptorObjectProvider = applicationContext.getBeanProvider(WalletAddInterceptor.class);
        ObjectProvider<WalletQueryInterceptor> queryInterceptorObjectProvider = applicationContext.getBeanProvider(WalletQueryInterceptor.class);

        WalletRemoveInterceptor removeInterceptor = removeInterceptorObjectProvider.getIfAvailable();
        WalletAddInterceptor addInterceptor = addInterceptorObjectProvider.getIfAvailable();
        WalletQueryInterceptor queryInterceptor = queryInterceptorObjectProvider.getIfAvailable();

        FabricDbWallet fabricDbWallet = new FabricDbWallet(context, jdbcTemplate, properties, applicationContext);
        if (removeInterceptor != null) {
            fabricDbWallet.setWalletRemoveInterceptor(removeInterceptor);
        }
        if (addInterceptor != null) {
            fabricDbWallet.setWalletAddInterceptor(addInterceptor);
        }
        if (queryInterceptor != null) {
            fabricDbWallet.setWalletQueryInterceptor(queryInterceptor);
        }
        return fabricDbWallet;
    }
}
