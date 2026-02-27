package com.tianrenservice.orch_framework.autoconfigure.aspect;

import com.tianrenservice.orch_framework.core.record.annotation.RecordAndReplay;
import com.tianrenservice.orch_framework.core.record.handler.RecordAndReplayHandler;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 录制/回放 AOP 切面 — 薄壳适配器
 *
 * 仅负责将 AspectJ ProceedingJoinPoint 适配为 RecordAndReplayHandler 的调用参数，
 * 全部业务逻辑委托给 RecordAndReplayHandler。
 */
@Aspect
@RequiredArgsConstructor
public class RecordAndReplayAspect {

    private final RecordAndReplayHandler handler;

    @Pointcut("@within(com.tianrenservice.orch_framework.core.record.annotation.RecordAndReplay) "
            + "|| @annotation(com.tianrenservice.orch_framework.core.record.annotation.RecordAndReplay)")
    public void recordAndReplayPointcut() {
    }

    @Around("recordAndReplayPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        return handler.handle(
                method.getName(),
                signature.getReturnType(),
                method.getAnnotation(RecordAndReplay.class),
                joinPoint.getTarget().getClass().getAnnotation(RecordAndReplay.class),
                joinPoint.getTarget(),
                joinPoint.getArgs(),
                joinPoint::proceed
        );
    }
}
