package com.tianrenservice.orch_framework.core.record.handler;

import com.tianrenservice.orch_framework.core.constant.BusinessMode;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.record.annotation.RecordAndReplay;
import com.tianrenservice.orch_framework.core.record.model.BusinessEnv;
import com.tianrenservice.orch_framework.core.record.model.InteractionRecord;
import com.tianrenservice.orch_framework.core.spi.JsonSerializer;
import com.tianrenservice.orch_framework.core.util.BeanUtil;
import com.tianrenservice.orch_framework.core.util.CacheInvoke;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 录制/回放核心处理器 - 纯 Java，无框架依赖
 *
 * 从原 RecordAndReplayAspect 中抽取的业务逻辑。
 * AOP 切面（如 Spring AOP）通过实现 InvocationContext 接口适配即可。
 */
@Slf4j
@RequiredArgsConstructor
public class RecordAndReplayHandler {

    @Delegate
    private final CacheInvoke cacheInvoke = new CacheInvoke(true);

    private final JsonSerializer jsonSerializer;

    /**
     * 方法调用上下文抽象 — 解耦 AspectJ ProceedingJoinPoint
     */
    @FunctionalInterface
    public interface MethodInvoker {
        Object invoke() throws Throwable;
    }

    /**
     * 核心录制/回放处理逻辑
     *
     * @param methodName      被拦截的方法名
     * @param returnType      方法返回类型
     * @param methodAnnotation 方法上的 @RecordAndReplay（可能为 null）
     * @param classAnnotation  类上的 @RecordAndReplay（可能为 null）
     * @param target          目标对象（必须是 BusinessEnv 或其子类）
     * @param args            方法参数
     * @param invoker         原方法调用回调
     * @return 方法执行结果
     */
    public Object handle(String methodName, Class<?> returnType,
                         RecordAndReplay methodAnnotation, RecordAndReplay classAnnotation,
                         Object target, Object[] args, MethodInvoker invoker) throws Throwable {

        List<Object> argList = new ArrayList<>(Arrays.asList(args));

        RecordAndReplay annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        String key = (annotation == null || annotation.value().isEmpty()) ? methodName : annotation.value();

        if ("ignore".equals(key)) {
            return invoker.invoke();
        }

        BusinessEnv businessEnv = (BusinessEnv) target;
        BusinessMode mode = businessEnv.getMode();

        if (mode == BusinessMode.LIVE) {
            return invoker.invoke();
        }

        if (annotation != null && !annotation.mark().isEmpty()) {
            argList.replaceAll(arg -> cacheInvoke(businessEnv, annotation.mark(),
                    Collections.singletonList(Object.class), arg));
        }
        argList.replaceAll(this::getDefaultMark);

        log.info("方法{}进行交互: args={}", methodName, jsonSerializer.toJson(argList));

        Object result = null;
        List<InteractionRecord> methodRecords;
        InteractionRecord record;
        boolean isVoidMethod = returnType.equals(Void.TYPE);

        switch (mode) {
            case RECORD:
                if (!isVoidMethod) {
                    result = invoker.invoke();
                } else {
                    invoker.invoke();
                }
                break;
            case CHECK:
            case REVIEW:
            case REGENERATE:
                methodRecords = businessEnv.getCovers().get(key);
                if (methodRecords == null || methodRecords.isEmpty()) {
                    log.error("未找到方法{}的记录", key);
                    throw new InterruptException("未找到方法" + key + "的记录");
                }
                if (isVoidMethod) {
                    businessEnv.findMatchingRecord(methodRecords, argList);
                } else {
                    record = businessEnv.findMatchingRecord(methodRecords, argList);
                    result = castResult(record, returnType);
                }
                break;
            case REPLAY:
                methodRecords = businessEnv.getCovers().get(key);
                if (methodRecords == null || methodRecords.isEmpty()) {
                    log.warn("未找到方法{}的记录，直接执行", key);
                    result = invoker.invoke();
                    break;
                }
                record = businessEnv.findMatchingRecordIgnoreNoFind(methodRecords, argList);
                if (record == null) {
                    log.warn("未找到方法{}的匹配记录，直接执行", key);
                    result = invoker.invoke();
                    break;
                }
                if (!isVoidMethod) {
                    result = castResult(record, returnType);
                }
                break;
            default:
                throw new InterruptException("未知模式: " + mode);
        }

        record = businessEnv.findMatchingRecordIgnoreNoFind(businessEnv.getRecords().get(key), argList);
        if (record == null) {
            record = new InteractionRecord(key, argList, result);
            businessEnv.getRecords().computeIfAbsent(key, k -> new ArrayList<>()).add(record);
            log.info("方法{}新增交互记录: {}", key, jsonSerializer.toJson(record));
        }
        return result;
    }

    private Object castResult(InteractionRecord record, Class<?> returnType) {
        Object result = record.getResult();
        if (result != null) {
            result = doCastResult(result, returnType);
            if (result == null) {
                throw new InterruptException("方法" + record.getMethodName()
                        + "记录的返回值=" + jsonSerializer.toJson(record.getResult()) + "，类型转换失败");
            }
        }
        return result;
    }

    public Object getDefaultMark(Object o) {
        if (o == null || BeanUtil.isPrimitive(o.getClass()) || o instanceof String || o instanceof Collection) {
            return o;
        }
        return jsonSerializer.toMap(jsonSerializer.toJson(o));
    }

    public Object doCastResult(Object result, Class<?> returnType) {
        if (result == null) return null;
        if (returnType.isAssignableFrom(result.getClass())) return result;
        if (returnType == Integer.class || returnType == int.class) return Integer.parseInt(result.toString());
        if (returnType == Long.class || returnType == long.class) return Long.parseLong(result.toString());
        if (returnType == Double.class || returnType == double.class) return Double.parseDouble(result.toString());
        if (returnType == Boolean.class || returnType == boolean.class) return Boolean.parseBoolean(result.toString());
        if (returnType == Float.class || returnType == float.class) return Float.parseFloat(result.toString());
        if (returnType == Short.class || returnType == short.class) return Short.parseShort(result.toString());
        if (returnType == Byte.class || returnType == byte.class) return Byte.parseByte(result.toString());
        if (returnType == Character.class || returnType == char.class) {
            if (result.toString().length() == 1) return result.toString().charAt(0);
            log.error("无法将结果转换为字符类型，结果长度不为1: {}", result);
            return null;
        }
        if (returnType == String.class) return result.toString();
        return jsonSerializer.fromJson(jsonSerializer.toJson(result), returnType);
    }
}
