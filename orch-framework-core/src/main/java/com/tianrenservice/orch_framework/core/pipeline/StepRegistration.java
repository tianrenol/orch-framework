package com.tianrenservice.orch_framework.core.pipeline;

import com.tianrenservice.orch_framework.core.entity.BusinessEntity;
import com.tianrenservice.orch_framework.core.vo.UserBusinessDealVO;
import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;

import java.util.Set;

/**
 * 步骤注册信息 - addStep() 时缓存的步骤元数据
 *
 * 框架内部使用，不暴露给消费方。
 */
record StepRegistration<V extends UserBusinessDealVO<T>, T extends BusinessEntity<?>, R extends UserBusinessVO>(
        BusinessFacade<V, T, R> facade,
        R input,
        Set<StepOutputKey<?>> requires,
        StepOutputKey<?> provides
) {
}
