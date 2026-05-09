package com.tianrenservice.orch_framework.core.spi;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * JSON 序列化契约 - 解耦具体 JSON 库依赖
 * 框架提供基于 Jackson 的默认实现
 */
public interface JsonSerializer {

    /**
     * 对象序列化为 JSON 字符串
     */
    String toJson(Object obj);

    /**
     * JSON 字符串反序列化为指定类型
     */
    <T> T fromJson(String json, Class<T> clazz);

    /**
     * JSON 字符串反序列化为指定泛型类型
     * <p>
     * 用于处理泛型返回值场景，如 {@code List<String>}、{@code Map<String, Object>} 等。
     * 配合 {@link TypeToken} 使用。
     */
    Object fromJson(String json, Type type);

    /**
     * JSON 字符串反序列化为 Map
     */
    Map<String, Object> toMap(String json);
}
