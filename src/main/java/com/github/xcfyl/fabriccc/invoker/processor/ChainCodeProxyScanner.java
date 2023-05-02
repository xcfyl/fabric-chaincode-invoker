package com.github.xcfyl.fabriccc.invoker.processor;

import com.github.xcfyl.fabriccc.invoker.annotation.ChainCodeProxy;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.Set;

/**
 * 负责扫描类路径下面的被标记为ChainCodeProxy的接口
 *
 * @author 西城风雨楼
 */
public class ChainCodeProxyScanner extends ClassPathBeanDefinitionScanner {

    public ChainCodeProxyScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }

    @Override
    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
        return metadataReader.getAnnotationMetadata().isInterface()
                && metadataReader.getAnnotationMetadata().hasAnnotation(ChainCodeProxy.class.getName());
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface()
                && beanDefinition.getMetadata().hasAnnotation(ChainCodeProxy.class.getName());
    }
}
