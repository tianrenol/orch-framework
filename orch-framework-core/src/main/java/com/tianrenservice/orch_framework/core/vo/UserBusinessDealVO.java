package com.tianrenservice.orch_framework.core.vo;

import com.tianrenservice.orch_framework.core.entity.Business;
import com.tianrenservice.orch_framework.core.entity.BusinessEntity;
import com.tianrenservice.orch_framework.core.entity.UserBusiness;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户级业务输出基类
 */
@Getter
@Slf4j
public abstract class UserBusinessDealVO<T extends BusinessEntity<?>> extends BusinessDealVO<T> {
    private final String userId;

    public UserBusinessDealVO(Business business) {
        super(business);
        userId = "";
    }

    public UserBusinessDealVO(UserBusiness userBusiness) {
        super(userBusiness);
        userId = userBusiness.getUserId();
    }

    public static <T extends BusinessDealVO<?>> T buildEmpty(Class<T> c) {
        try {
            return c.getConstructor(UserBusiness.class).newInstance(new UserBusiness(""));
        } catch (Exception e) {
            throw new InterruptException("构建空处理结果失败", e);
        }
    }
}
