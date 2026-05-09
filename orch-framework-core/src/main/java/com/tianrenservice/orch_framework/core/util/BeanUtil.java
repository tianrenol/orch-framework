package com.tianrenservice.orch_framework.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Bean 工具类 - 从原项目 BeanUtil 中提取框架所需的最小子集
 */
public class BeanUtil {

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(boolean.class, Boolean.class);
        primitiveWrapperMap.put(byte.class, Byte.class);
        primitiveWrapperMap.put(char.class, Character.class);
        primitiveWrapperMap.put(double.class, Double.class);
        primitiveWrapperMap.put(float.class, Float.class);
        primitiveWrapperMap.put(int.class, Integer.class);
        primitiveWrapperMap.put(long.class, Long.class);
        primitiveWrapperMap.put(short.class, Short.class);
        primitiveWrapperMap.put(void.class, Void.class);
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || primitiveWrapperMap.containsKey(clazz)
                || new HashSet<>(primitiveWrapperMap.values()).contains(clazz);
    }

    public static Class<?> getWrapperClass(Class<?> primitiveClass) {
        return primitiveWrapperMap.get(primitiveClass);
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz, Object object) {
        return clazz.isInstance(object)
                || (clazz.isPrimitive() && getWrapperClass(clazz) != null && getWrapperClass(clazz).isInstance(object));
    }
}
