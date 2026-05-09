package com.tianrenservice.orch_framework.core.vo;

import com.tianrenservice.orch_framework.core.entity.Business;
import com.tianrenservice.orch_framework.core.spi.BusinessTypeIdentifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务输入基类
 *
 * 兼容性改动: getBusinessEnum() → getBusinessType()
 * 原返回 BusinessEnum 枚举，现返回 BusinessTypeIdentifier 接口
 * 业务方需将枚举实现 BusinessTypeIdentifier 接口即可适配
 */
@Getter
@Slf4j
public abstract class BusinessVO {
    private final String identity;

    public BusinessVO(Business business) {
        identity = business.getIdentity();
    }

    /**
     * 返回业务类型标识
     * 兼容性改动: 原方法名 getBusinessEnum()，返回类型从 BusinessEnum 改为 BusinessTypeIdentifier
     */
    public abstract BusinessTypeIdentifier getBusinessType();
}
