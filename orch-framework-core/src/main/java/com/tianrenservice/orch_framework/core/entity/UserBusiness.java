package com.tianrenservice.orch_framework.core.entity;

import lombok.Getter;

/**
 * 用户级业务实体 - 携带 userId
 */
@Getter
public class UserBusiness extends Business {
    private final String userId;

    public UserBusiness(String userId) {
        this.userId = userId;
    }
}
