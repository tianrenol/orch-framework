package com.tianrenservice.orch_framework.core.vo;

import com.tianrenservice.orch_framework.core.entity.Business;
import com.tianrenservice.orch_framework.core.entity.UserBusiness;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户级业务输入基类
 */
@Setter
@Getter
@Slf4j
public abstract class UserBusinessVO extends BusinessVO {
    private String userId;

    public UserBusinessVO(Business business) {
        super(business);
        userId = "";
    }

    public UserBusinessVO(UserBusiness userBusiness) {
        super(userBusiness);
        userId = userBusiness.getUserId();
    }

    public static <T extends BusinessVO> T buildEmpty(Class<T> c) {
        try {
            return c.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InterruptException("构建空业务输入失败", e);
        }
    }
}
