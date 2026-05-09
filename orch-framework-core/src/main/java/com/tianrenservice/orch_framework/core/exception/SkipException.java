package com.tianrenservice.orch_framework.core.exception;

/**
 * 跳过异常 - 跳过当前操作，不中断整体流程
 */
public class SkipException extends BusinessException {
    private static final long serialVersionUID = -1839135916794151642L;

    public SkipException(String message) {
        super(message);
    }

    public SkipException(String message, Throwable cause) {
        super(message, cause);
    }
}
