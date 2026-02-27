package com.tianrenservice.ai_framework_spring.core.pipeline;

import lombok.Getter;

/**
 * 通用动态装配线 — 替代消费方编写空壳 Assembly 子类
 * 消费方不再需要为每个业务流程创建单独的 Assembly 类
 */
@Getter
public class DynamicAssembly extends BusinessAssembly {

    private final String typeCode;

    public DynamicAssembly(String typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    public String getAssemblyTypeCode() {
        return typeCode;
    }
}
