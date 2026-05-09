package com.tianrenservice.orch_framework.autoconfigure;

import com.tianrenservice.orch_framework.autoconfigure.aspect.RecordAndReplayAspect;
import com.tianrenservice.orch_framework.autoconfigure.spi.DefaultTypeRegistry;
import com.tianrenservice.orch_framework.autoconfigure.spi.JacksonJsonSerializer;
import com.tianrenservice.orch_framework.autoconfigure.spi.SpringBeanProvider;
import com.tianrenservice.orch_framework.core.entity.BusinessHelper;
import com.tianrenservice.orch_framework.core.pipeline.BusinessAssembly;
import com.tianrenservice.orch_framework.core.pipeline.BusinessFacade;
import com.tianrenservice.orch_framework.core.record.handler.RecordAndReplayHandler;
import com.tianrenservice.orch_framework.core.record.model.BusinessEnv;
import com.tianrenservice.orch_framework.core.spi.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * Orch Framework Spring Boot 自动配置
 *
 * 自动装配所有 SPI 默认实现，并完成框架初始化：
 * - JsonSerializer → JacksonJsonSerializer
 * - BeanProvider → SpringBeanProvider
 * - TypeRegistry → DefaultTypeRegistry
 * - ExceptionHandler → DefaultExceptionHandler
 * - RecordAndReplayHandler → 录制/回放核心处理器
 * - RecordAndReplayAspect → AOP 切面（薄壳）
 * - BusinessEnv → Prototype Bean
 *
 * 业务方可通过自定义 Bean 覆盖任意默认实现
 */
@AutoConfiguration
@EnableConfigurationProperties(OrchFrameworkProperties.class)
public class OrchFrameworkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JsonSerializer jsonSerializer() {
        return new JacksonJsonSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExceptionHandler exceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanProvider beanProvider(ApplicationContext applicationContext) {
        SpringBeanProvider provider = new SpringBeanProvider(applicationContext);
        BusinessHelper.configureBeanProvider(provider);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeRegistry typeRegistry(JsonSerializer jsonSerializer,
                                     ExceptionHandler exceptionHandler,
                                     ObjectProvider<List<BusinessTypeIdentifier>> businessTypes,
                                     ObjectProvider<List<AssemblyTypeIdentifier>> assemblyTypes) {
        DefaultTypeRegistry registry = new DefaultTypeRegistry();
        businessTypes.ifAvailable(types ->
                types.forEach(registry::registerBusinessType));
        assemblyTypes.ifAvailable(types ->
                types.forEach(registry::registerAssemblyType));
        BusinessAssembly.configure(registry, jsonSerializer);
        BusinessFacade.configureExceptionHandler(exceptionHandler);
        return registry;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean
    public BusinessEnv businessEnv() {
        return new BusinessEnv();
    }

    @Bean
    @ConditionalOnMissingBean
    public RecordAndReplayHandler recordAndReplayHandler(JsonSerializer jsonSerializer) {
        return new RecordAndReplayHandler(jsonSerializer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "orch-framework", name = "aspect-enabled", havingValue = "true", matchIfMissing = true)
    public RecordAndReplayAspect recordAndReplayAspect(RecordAndReplayHandler handler) {
        return new RecordAndReplayAspect(handler);
    }
}
