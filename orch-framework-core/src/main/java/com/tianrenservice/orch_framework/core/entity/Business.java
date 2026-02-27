package com.tianrenservice.orch_framework.core.entity;

import lombok.Getter;

import java.util.UUID;

/**
 * 业务根实体 - 提供唯一链路标识
 */
@Getter
public class Business {
    private final String identity = "business-" + UUID.randomUUID();
}
