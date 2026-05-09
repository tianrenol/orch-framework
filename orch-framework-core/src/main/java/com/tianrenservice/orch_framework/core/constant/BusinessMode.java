package com.tianrenservice.orch_framework.core.constant;

/**
 * 业务模式枚举 - 控制框架运行模式
 */
public enum BusinessMode {
    RECORD("记录"),
    REGENERATE("重生成"),
    REPLAY("重播"),
    CHECK("检查"),
    REVIEW("复盘"),
    LIVE("实时"),
    ;

    private final String desc;

    BusinessMode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
