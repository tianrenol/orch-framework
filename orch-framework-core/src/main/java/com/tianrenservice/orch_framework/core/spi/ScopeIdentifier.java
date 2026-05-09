package com.tianrenservice.orch_framework.core.spi;

/**
 * 作用域标识契约 - 替代原 ScopeEnum 硬编码枚举
 * 业务方通过实现此接口定义自己的作用域
 */
public interface ScopeIdentifier {

    /**
     * 作用域编码
     */
    String getCode();

    /**
     * 作用域描述
     */
    String getDescription();
}
