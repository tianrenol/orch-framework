package com.tianrenservice.ai_framework_spring.core.spi;

import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;

/**
 * BusinessTypeIdentifier 的简单实现 — 替代消费方编写枚举类
 */
public record SimpleBusinessType(
        String code,
        String description,
        Class<? extends UserBusinessVO> voClass
) implements BusinessTypeIdentifier {

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Class<? extends UserBusinessVO> getVoClass() {
        return voClass;
    }
}
