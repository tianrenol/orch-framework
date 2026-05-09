package com.tianrenservice.orch_framework.core.vo;

import com.tianrenservice.orch_framework.core.entity.Business;
import com.tianrenservice.orch_framework.core.entity.BusinessEntity;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.spi.ScopeIdentifier;
import com.tianrenservice.orch_framework.core.util.BusinessFutureTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 业务输出基类
 *
 * 兼容性改动: scope 字段类型从 ScopeEnum 改为 ScopeIdentifier 接口
 */
@Getter
@Slf4j
public abstract class BusinessDealVO<T extends BusinessEntity<?>> {
    private final String identity;
    private ScopeIdentifier scope;
    private long nowTime;
    private final List<BusinessFutureTask<?>> ALL_TASKS = new CopyOnWriteArrayList<>();

    public BusinessDealVO(Business business) {
        identity = business.getIdentity();
    }

    public void addFutureTask(BusinessFutureTask<?> task) {
        ALL_TASKS.add(task);
    }

    public <V extends BusinessDealVO<T>> V build(T business) {
        scope = business.getScopeIdentifier();
        nowTime = business.getNowTime();
        return doBuild(business);
    }

    public abstract <V extends BusinessDealVO<T>> V doBuild(T business);

    public void executeAllFutureTask() {
        for (BusinessFutureTask<?> task : ALL_TASKS) {
            try {
                task.get();
            } catch (Exception e) {
                log.error("BusinessDealVO futureTask execute error, identity={}", identity, e);
                throw new InterruptException("BusinessDealVO futureTask execute error", e);
            }
        }
    }
}
