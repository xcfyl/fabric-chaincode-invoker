package com.github.xcfyl.fabriccc.invoker.processor;

import com.github.xcfyl.fabriccc.invoker.annotation.EnableChainCodeProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

/**
 * 负责将扫描到的ChainCodeProxy对应的FactoryBean注册到Spring容器中
 *
 * @author 西城风雨楼
 */
public class ChainCodeProxyImportSelector implements ImportSelector, BeanFactoryAware {
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableChainCodeProxy.class.getName());
        if (attributes == null || attributes.size() == 0) {
            return new String[0];
        }

        String[] scanPackages = (String[]) attributes.get("scanPackages");
        ChainCodeProxyScanner scanner = new ChainCodeProxyScanner((BeanDefinitionRegistry) beanFactory);
        Set<BeanDefinitionHolder> definitionHolders = scanner.doScan(scanPackages);
        for (BeanDefinitionHolder holder : definitionHolders) {
            BeanDefinition beanDefinition = holder.getBeanDefinition();
            String className = beanDefinition.getBeanClassName();
            try {
                Class<?> proxyInterface = Class.forName(className);
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(proxyInterface);
                beanDefinition.setBeanClassName(ChainCodeProxyFactoryBean.class.getName());
            } catch (Exception e) {
                throw new RuntimeException("解析ChainCodeProxy错误");
            }
        }
        return new String[0];
    }
}
