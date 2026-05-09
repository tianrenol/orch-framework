package com.tianrenservice.orch_framework.core.spi;

import com.tianrenservice.orch_framework.core.exception.DegradeException;
import com.tianrenservice.orch_framework.core.exception.SkipException;
import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;

/**
 * 异常处理契约 - Pipeline 执行过程中的异常处理策略
 *
 * 框架提供 {@link DefaultExceptionHandler} 作为默认实现，
 * 业务方可声明自定义 {@code @Bean} 覆盖以介入异常处理流程。
 */
public interface ExceptionHandler {

    /**
     * 跳过异常处理
     *
     * @param e     跳过异常
     * @param input 当前业务输入
     * @return Pipeline 的替代返回值（默认返回 null）
     */
    <V> V onSkip(SkipException e, UserBusinessVO input);

    /**
     * 降级异常处理
     *
     * @param e     降级异常（可调用 {@link DegradeException#degrade()} 执行降级逻辑）
     * @param input 当前业务输入
     * @return Pipeline 的替代返回值（默认调用 e.degrade() 后返回 null）
     */
    <V> V onDegrade(DegradeException e, UserBusinessVO input);

    /**
     * 未预期异常处理
     *
     * @param e     原始异常
     * @param input 当前业务输入
     * @return Pipeline 的替代返回值，或直接抛出异常
     */
    <V> V onException(Exception e, UserBusinessVO input);

    /**
     * Pipeline 完成后回调（无论成功或异常），用于资源清理、监控打点等
     *
     * @param input 当前业务输入
     */
    default void onFinally(UserBusinessVO input) {
    }
}
