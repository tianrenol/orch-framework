package com.tianrenservice.orch_framework.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Orch Framework 配置属性
 */
@ConfigurationProperties(prefix = "orch-framework")
public class OrchFrameworkProperties {

    /**
     * 是否启用录制/回放功能
     */
    private boolean recordEnabled = true;

    /**
     * 是否启用 AOP 切面
     */
    private boolean aspectEnabled = true;

    public boolean isRecordEnabled() {
        return recordEnabled;
    }

    public void setRecordEnabled(boolean recordEnabled) {
        this.recordEnabled = recordEnabled;
    }

    public boolean isAspectEnabled() {
        return aspectEnabled;
    }

    public void setAspectEnabled(boolean aspectEnabled) {
        this.aspectEnabled = aspectEnabled;
    }
}
