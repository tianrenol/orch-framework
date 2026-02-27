package com.tianrenservice.ai_framework_spring.core.pipeline;

import com.tianrenservice.ai_framework_spring.core.entity.BusinessEntity;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessDealVO;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;
import lombok.Getter;

import java.util.Objects;

/**
 * 组合线单元 - Pipeline 中的单个执行节点
 */
@Getter
public class BusinessAssemblyUnit<V extends UserBusinessDealVO<?>, T extends BusinessEntity<?>, R extends UserBusinessVO> {

    private final BusinessContext<R> businessContext;
    private T businessEntity;
    private V businessDealVO;
    private final int order;

    public BusinessAssemblyUnit(BusinessContext<R> businessContext, T businessEntity, V businessDealVO, int order) {
        this.order = order;
        if (Objects.isNull(businessContext)) {
            throw new InterruptException("BusinessContext cannot be null");
        }
        this.businessContext = businessContext;
        this.businessEntity = businessEntity;
        this.businessDealVO = businessDealVO;
    }

    @SuppressWarnings("unchecked")
    public void ready(BusinessEntity<?> t) {
        if (Objects.nonNull(businessEntity)) {
            throw new InterruptException("businessEntity is already set, cannot be overwritten.");
        }
        this.businessEntity = (T) t;
    }

    public boolean isReady() {
        return Objects.nonNull(businessEntity);
    }

    @SuppressWarnings("unchecked")
    public void complete(UserBusinessDealVO<?> v) {
        if (Objects.nonNull(businessDealVO)) {
            throw new InterruptException("BusinessDealVO is already set, cannot be overwritten.");
        }
        this.businessDealVO = (V) v;
    }

    public boolean isComplete() {
        return Objects.nonNull(businessDealVO);
    }

    public String getMarkName() {
        return businessContext.getAssembly().getAssemblyTypeCode()
                + "-" + businessContext.getBusinessVo().getBusinessType().getCode()
                + "-" + order;
    }

    public static <V extends UserBusinessDealVO<?>, T extends BusinessEntity<?>, R extends UserBusinessVO>
    BusinessAssemblyUnit<V, T, R> doBuild(Class<V> vClass, Class<T> tClass, R r, BusinessAssembly a) {
        return new BusinessAssemblyUnit<>(BusinessContext.build(r, a), null, null, a.getUnitCount());
    }

    public static <V extends UserBusinessDealVO<?>, T extends BusinessEntity<?>, R extends UserBusinessVO>
    BusinessAssemblyUnit<V, T, R> doBuild(V v, T t, R r, BusinessAssembly a) {
        return new BusinessAssemblyUnit<>(BusinessContext.build(r, a), t, v, a.getUnitCount());
    }
}
