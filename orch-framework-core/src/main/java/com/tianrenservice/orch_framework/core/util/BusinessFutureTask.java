package com.tianrenservice.orch_framework.core.util;

import com.tianrenservice.orch_framework.core.vo.BusinessDealVO;
import lombok.Getter;

import java.util.concurrent.Callable;

/**
 * 业务 FutureTask - 自动注册到 BusinessDealVO 的任务列表
 */
@Getter
public class BusinessFutureTask<V> extends LazyFutureTask<V> {

    public BusinessFutureTask(BusinessDealVO<?> sourceDealVO, Callable<V> callable) {
        super(callable);
        sourceDealVO.addFutureTask(this);
    }
}
