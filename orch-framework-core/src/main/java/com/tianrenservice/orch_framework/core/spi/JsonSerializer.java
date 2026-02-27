package com.tianrenservice.orch_framework.core.spi;

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
     * JSON 字符串反序列化为 Map
     */
    Map<String, Object> toMap(String json);
}
