package com.tianrenservice.orch_framework.core.spi;

/**
 * Bean 提供者契约 - 解耦 Spring ApplicationContext 硬依赖
 * 框架提供基于 Spring 的默认实现
 */
public interface BeanProvider {

    /**
     * 获取指定类型的 Bean 实例
     */
    <T> T getBean(Class<T> clazz);
}
