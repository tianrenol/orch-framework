package com.tianrenservice.orch_framework.autoconfigure.spi;

import com.tianrenservice.orch_framework.core.spi.BeanProvider;
import org.springframework.context.ApplicationContext;

/**
 * BeanProvider 的 Spring ApplicationContext 默认实现
 */
public class SpringBeanProvider implements BeanProvider {

    private final ApplicationContext applicationContext;

    public SpringBeanProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
