package com.github.xcfyl.fabriccc.invoker.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * @author 西城风雨楼
 */
public class ContainsDbTypeWalletCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        return environment.containsProperty("fabric.wallet.type")
                && Objects.equals(environment.getProperty("fabric.wallet.type"), "db");
    }
}
