package com.tianrenservice.orch_framework.core.util;

import com.tianrenservice.orch_framework.core.exception.InterruptException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 缓存反射调用工具 - 缓存 Method 对象以提升反射性能
 */
@Setter
@Slf4j
public class CacheInvoke {

    private static final Map<String, Method> cacheMethod = new ConcurrentHashMap<>();

    private final boolean isStrict;

    public CacheInvoke(boolean isStrict) {
        this.isStrict = isStrict;
    }

    public Object cacheInvoke(Object service, String methodName, List<Class<?>> parameterTypes, Object... args) {
        return cacheInvoke(isStrict, service, methodName, parameterTypes, args);
    }

    public static Object cacheInvoke(boolean isStrict, Object service, String methodName, List<Class<?>> parameterTypes, Object... args) {
        String key = Arrays.toString(
                Stream.concat(Stream.of(service.getClass(), methodName), parameterTypes.stream()).toArray()
        );
        String classMethodName = service.getClass().getName() + "." + methodName;
        Method handle;
        if (cacheMethod.containsKey(key)) {
            handle = cacheMethod.get(key);
        } else {
            try {
                handle = service.getClass().getMethod(methodName, parameterTypes.toArray(new Class[0]));
            } catch (NoSuchMethodException e) {
                if (isStrict) {
                    throw new InterruptException("method " + methodName + " no exist", e);
                }
                log.warn("{} method not found, {}", classMethodName, e.getMessage());
                return null;
            }
            cacheMethod.put(key, handle);
        }
        try {
            return handle.invoke(service, args);
        } catch (InterruptException | InvocationTargetException e) {
            throw new InterruptException("invoke " + classMethodName + " error", e);
        } catch (IllegalAccessException e) {
            if (isStrict) {
                throw new InterruptException("invoke " + classMethodName + " illegal access", e);
            }
            log.warn("{} method illegal access, {}", classMethodName, e.getMessage());
            return null;
        }
    }
}