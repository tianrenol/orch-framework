package com.tianrenservice.orch_framework.core.exception;

/**
 * 框架基础异常
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 8994314256512499415L;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BusinessException(String message, Object... args) {
        super(String.format(message, args));
    }

    public BusinessException(Throwable cause, String message, Object... args) {
        super(String.format(message, args), cause);
    }
}
