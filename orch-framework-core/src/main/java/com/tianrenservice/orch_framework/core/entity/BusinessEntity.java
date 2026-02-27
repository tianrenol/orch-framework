package com.tianrenservice.orch_framework.core.entity;

import com.tianrenservice.orch_framework.core.annotation.AfterProcess;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.record.model.BusinessEnv;
import com.tianrenservice.orch_framework.core.spi.ScopeIdentifier;
import com.tianrenservice.orch_framework.core.vo.BusinessDealVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 业务实体基类 - 承载业务逻辑的核心容器
 */
@Slf4j
@Getter
public class BusinessEntity<O extends BusinessHelper<?>> extends UserBusiness {

    protected final O businessHelper;

    public BusinessEntity(O businessHelper) {
        super(businessHelper.getUserId());
        this.businessHelper = businessHelper;
    }

    public long getNowTime() {
        return businessHelper.getNowTime();
    }

    /**
     * 返回作用域标识，子类按需覆写
     */
    public ScopeIdentifier getScopeIdentifier() {
        return null;
    }

    /**
     * Env 快捷访问 — 避免 getBusinessHelper().getBusinessEnv(clazz) 长链路
     */
    public <E extends BusinessEnv> E getEnv(Class<E> envClass) {
        return businessHelper.getBusinessEnv(envClass);
    }

    public <V extends BusinessDealVO<T>, T extends BusinessEntity<?>> V buildVO(Class<V> clazz) {
        try {
            return clazz.getConstructor(UserBusiness.class).newInstance(this).build((T) this);
        } catch (Exception e) {
            throw new InterruptException("构造处理结果失败", e);
        }
    }

    /**
     * Pipeline 后处理 — 优先执行 @AfterProcess 注解方法，回退到 Helper 的同名方法
     */
    public void afterProcess() {
        boolean hasSaveDB = invokeAnnotatedMethods(AfterProcess.Phase.SAVE_DB);
        boolean hasDelRedis = invokeAnnotatedMethods(AfterProcess.Phase.DEL_REDIS);

        if (!hasSaveDB) {
            getBusinessHelper().saveDB();
        }
        if (!hasDelRedis) {
            getBusinessHelper().delRedis();
        }
    }

    /**
     * Pipeline 完成回调 — 优先执行 @AfterProcess(FINISH)，回退到 Helper.finish()
     */
    public void finish() {
        boolean hasFinish = invokeAnnotatedMethods(AfterProcess.Phase.FINISH);
        if (!hasFinish) {
            businessHelper.finish();
        }
    }

    /**
     * 扫描并执行当前实体上标注了 @AfterProcess 的方法
     */
    private boolean invokeAnnotatedMethods(AfterProcess.Phase phase) {
        boolean found = false;
        for (Method m : this.getClass().getDeclaredMethods()) {
            AfterProcess ann = m.getAnnotation(AfterProcess.class);
            if (ann != null && ann.value() == phase) {
                try {
                    m.setAccessible(true);
                    m.invoke(this);
                    found = true;
                } catch (Exception e) {
                    throw new InterruptException("执行 @AfterProcess(" + phase + ") 失败: " + m.getName(), e);
                }
            }
        }
        return found;
    }
}
