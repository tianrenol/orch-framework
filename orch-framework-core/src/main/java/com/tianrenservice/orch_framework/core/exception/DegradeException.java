package com.tianrenservice.orch_framework.core.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 降级异常 - 触发降级逻辑
 */
@Slf4j
public class DegradeException extends BusinessException {
    private static final long serialVersionUID = -6086893158279321093L;

    Runnable degrade;

    public DegradeException(String message, Runnable degrade) {
        super(message);
        this.degrade = degrade;
    }

    public DegradeException(String message, Throwable cause, Runnable degrade) {
        super(message, cause);
        this.degrade = degrade;
    }

    public void degrade() {
        log.warn("Degrade logic executed for exception: {}", getMessage());
        if (degrade != null) {
            try {
                degrade.run();
            } catch (Exception e) {
                throw new InterruptException("降级处理失败", e);
            }
        }
    }
}
