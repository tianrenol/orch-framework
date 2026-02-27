package com.tianrenservice.orch_framework.core.record.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要录制和回放的方法或类
 * 标注在方法上：该方法的调用会被录制/回放
 * 标注在类上：该类所有方法都会被录制/回放
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordAndReplay {
    /**
     * 方法标识，默认使用方法名
     */
    String value() default "";

    /**
     * 参数标识方法名，默认使用框架生成策略
     */
    String mark() default "";
}
