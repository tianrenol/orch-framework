package com.tianrenservice.orch_framework.core.spi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型类型令牌 — 用于在运行时保留泛型信息
 * <p>
 * 使用方式（匿名子类）：
 * <pre>
 *   TypeToken&lt;List&lt;String&gt;&gt; token = new TypeToken&lt;List&lt;String&gt;&gt;() {};
 * </pre>
 * 类似于 Jackson 的 TypeReference，但无框架依赖。
 *
 * @param <T> 目标泛型类型
 */
public abstract class TypeToken<T> {

    private final Type type;

    protected TypeToken() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType pt) {
            this.type = pt.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("TypeToken must be created with actual type parameter");
        }
    }

    public Type getType() {
        return type;
    }
}
