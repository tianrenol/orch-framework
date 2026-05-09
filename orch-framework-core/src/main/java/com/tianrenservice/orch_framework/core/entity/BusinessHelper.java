package com.tianrenservice.orch_framework.core.entity;

import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.pipeline.BusinessContext;
import com.tianrenservice.orch_framework.core.record.model.BusinessEnv;
import com.tianrenservice.orch_framework.core.spi.BeanProvider;
import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * 业务辅助基类 - 管理 Context、Env，提供生命周期钩子
 */
@Slf4j
@Setter
public class BusinessHelper<R extends UserBusinessVO> {

    protected BusinessContext<R> businessContext;
    protected BusinessEnv businessEnv;

    private static BeanProvider beanProvider;

    /**
     * 框架初始化时注入 BeanProvider
     */
    public static void configureBeanProvider(BeanProvider provider) {
        beanProvider = provider;
    }

    public String getUserId() {
        String uid = businessContext.getUserId();
        return uid == null ? "" : uid;
    }

    public static <T extends BusinessEntity<?>, O extends BusinessHelper<?>> T build(O businessHelper, Class<T> clazz) {
        try {
            return clazz.getConstructor(businessHelper.getClass()).newInstance(businessHelper);
        } catch (InterruptException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new InterruptException("构造业务实体失败", e);
        }
    }

    public BusinessEnv getBusinessEnv() {
        return getBusinessEnv(BusinessEnv.class);
    }

    public long getNowTime() {
        return getBusinessEnv().getNowTime();
    }

    @SuppressWarnings("unchecked")
    public <E extends BusinessEnv> E getBusinessEnv(Class<E> eClass) {
        if (businessEnv == null) {
            if (beanProvider == null) {
                throw new InterruptException("BeanProvider 未配置，请调用 BusinessHelper.configureBeanProvider()");
            }
            businessEnv = beanProvider.getBean(eClass);
            businessEnv.setTestMode(
                    businessContext.getAssembly().getMode(),
                    businessContext.getAssembly().getTestCaseName(),
                    Optional.ofNullable(businessContext.getAssembly().getMultiBusinessEnv())
                            .map(env -> env.get(businessContext.getAssembly().getCurrentUnit().getMarkName()))
                            .orElse(null)
            );
        }
        assert businessEnv != null : "businessEnv is null!";
        return (E) businessEnv;
    }

    public BusinessContext<R> getBusinessContext() {
        return businessContext;
    }

    public void saveDB() {
    }

    public void delRedis() {
    }

    public void finish() {
    }
}
