package com.tianrenservice.orch_framework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Entity 上的生命周期钩子方法
 * 替代在 Helper 中覆写 saveDB()/delRedis()/finish()
 *
 * 使用方式:
 * <pre>
 * public class OrderEntity extends BusinessEntity&lt;...&gt; {
 *     &#64;AfterProcess(Phase.SAVE_DB)
 *     public void save() { repository.save(this); }
 *
 *     &#64;AfterProcess(Phase.DEL_REDIS)
 *     public void clearCache() { redis.delete("key"); }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterProcess {

    Phase value();

    enum Phase {
        SAVE_DB,
        DEL_REDIS,
        FINISH
    }
}
