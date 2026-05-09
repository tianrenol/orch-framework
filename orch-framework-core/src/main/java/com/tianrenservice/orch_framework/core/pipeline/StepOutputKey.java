package com.tianrenservice.orch_framework.core.pipeline;

import java.util.Objects;

/**
 * 步骤输出键 - 类型安全的异构容器键（Typesafe Heterogeneous Container 模式）
 *
 * 用于 Pipeline 步骤间的类型安全数据传递。
 * 消费方定义为 public static final 常量。
 *
 * @param <T> 输出值的类型
 */
public final class StepOutputKey<T> {

    private final String name;
    private final Class<T> type;

    private StepOutputKey(String name, Class<T> type) {
        this.name = Objects.requireNonNull(name, "StepOutputKey name 不能为空");
        this.type = Objects.requireNonNull(type, "StepOutputKey type 不能为空");
    }

    public static <T> StepOutputKey<T> of(String name, Class<T> type) {
        return new StepOutputKey<>(name, type);
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepOutputKey<?> that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "StepOutputKey[" + name + ":" + type.getSimpleName() + "]";
    }
}
