package com.tianrenservice.orch_framework.core.exception;

import java.util.Objects;
import java.util.Optional;

/**
 * 业务中断异常 - 中断当前业务流程
 */
public class InterruptException extends BusinessException {
    private static final long serialVersionUID = -8939472682015305971L;

    public InterruptException(String message) {
        super(message);
    }

    public InterruptException(String message, Throwable cause) {
        super("[" + message + "] ==> " + getInterruptMessage(cause, cause.getMessage()), cause);
    }

    public String getMessage() {
        return super.getMessage();
    }

    public static Throwable getInterruptWarp(Throwable cause) {
        if (Objects.isNull(cause)) {
            return null;
        }
        if (cause.getCause() instanceof InterruptException) {
            return cause.getCause();
        }
        return getInterruptWarp(cause.getCause());
    }

    public static String getInterruptMessage(Throwable cause, String defaultMsg) {
        return Optional.ofNullable(getInterruptWarp(cause)).map(Throwable::getMessage).orElse(defaultMsg);
    }
}
