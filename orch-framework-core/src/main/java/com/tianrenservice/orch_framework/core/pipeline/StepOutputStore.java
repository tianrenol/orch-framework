package com.tianrenservice.orch_framework.core.pipeline;

import com.tianrenservice.orch_framework.core.exception.InterruptException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 步骤输出存储 - 类型安全的异构容器
 *
 * 存储 Pipeline 各步骤的输出，支持类型安全的发布和获取。
 * 由 BusinessAssembly 持有，生命周期跟随 Assembly。
 */
public class StepOutputStore {

    private final Map<StepOutputKey<?>, Object> outputs = new LinkedHashMap<>();

    /**
     * 发布步骤输出
     *
     * @param key   输出键
     * @param value 输出值
     * @throws InterruptException 如果相同 key 已发布（防止覆盖）
     */
    public <T> void publish(StepOutputKey<T> key, T value) {
        if (key == null) {
            throw new InterruptException("发布步骤输出失败: key 不能为空");
        }
        if (outputs.containsKey(key)) {
            throw new InterruptException("步骤输出键 '" + key.name() + "' 已发布，不允许重复发布");
        }
        outputs.put(key, value);
    }

    /**
     * 获取步骤输出（必须存在）
     *
     * @param key 输出键
     * @return 类型安全的输出值
     * @throws InterruptException 如果 key 尚未发布
     */
    public <T> T require(StepOutputKey<T> key) {
        if (!outputs.containsKey(key)) {
            throw new InterruptException("步骤输出键 '" + key.name()
                    + "' 尚未发布，请确认前置步骤已执行。已发布的键: " + publishedKeys());
        }
        return key.type().cast(outputs.get(key));
    }

    /**
     * 获取步骤输出（可选，不存在返回 null）
     */
    public <T> T get(StepOutputKey<T> key) {
        Object value = outputs.get(key);
        return value != null ? key.type().cast(value) : null;
    }

    /**
     * 检查 key 是否已发布
     */
    public boolean contains(StepOutputKey<?> key) {
        return outputs.containsKey(key);
    }

    /**
     * 已发布的所有 key（只读视图）
     */
    public Set<StepOutputKey<?>> publishedKeys() {
        return Collections.unmodifiableSet(outputs.keySet());
    }
}
