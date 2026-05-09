package com.tianrenservice.orch_framework.core.spi;

/**
 * 组合线类型标识契约 - 替代原 AssemblyEnum 硬编码枚举
 */
public interface AssemblyTypeIdentifier {

    /**
     * 组合线类型编码
     */
    String getCode();

    /**
     * 组合线描述
     */
    String getDescription();

    /**
     * 对应的 Assembly 类
     */
    Class<? extends com.tianrenservice.orch_framework.core.pipeline.BusinessAssembly> getAssemblyClass();
}
