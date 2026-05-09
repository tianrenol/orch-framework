package com.tianrenservice.orch_framework.core.spi;

import com.tianrenservice.orch_framework.core.exception.DegradeException;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.exception.SkipException;
import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;
import lombok.extern.slf4j.Slf4j;

/**
 * ExceptionHandler 默认实现 - 保持框架原有行为
 *
 * 放在 core 模块中，非 Spring 环境也可直接使用。
 * Spring 环境下通过 {@code @ConditionalOnMissingBean} 自动装配，
 * 业务方声明自定义 Bean 即可覆盖。
 */
@Slf4j
public class DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public <V> V onSkip(SkipException e, UserBusinessVO input) {
        log.error("businessFacade process skip, userId:{}, message:{}", input.getUserId(), e.getMessage());
        return null;
    }

    @Override
    public <V> V onDegrade(DegradeException e, UserBusinessVO input) {
        log.error("businessFacade process degrade, userId:{}, message:{}", input.getUserId(), e.getMessage());
        e.degrade();
        return null;
    }

    @Override
    public <V> V onException(Exception e, UserBusinessVO input) {
        throw new InterruptException("businessFacade process interrupt", e);
    }
}
