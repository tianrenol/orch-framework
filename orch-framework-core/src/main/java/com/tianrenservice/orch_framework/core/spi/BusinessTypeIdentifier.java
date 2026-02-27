package com.tianrenservice.orch_framework.core.spi;

import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;

/**
 * 业务类型标识契约 - 替代原 BusinessEnum 硬编码枚举
 * 业务方通过实现此接口注册自己的业务类型
 */
public interface BusinessTypeIdentifier {

    /**
     * 业务类型编码（唯一标识）
     */
    String getCode();

    /**
     * 业务描述
     */
    String getDescription();

    /**
     * 对应的 VO 类型
     */
    Class<? extends UserBusinessVO> getVoClass();
}
