package com.tianrenservice.orch_framework.core.pipeline;

import lombok.Getter;
import lombok.Setter;

/**
 * 空组合线 - 默认/占位用途
 */
@Setter
@Getter
public class BusinessEmptyAssembly extends BusinessAssembly {

    private static final String EMPTY_CODE = "empty";

    @Override
    public String getAssemblyTypeCode() {
        return EMPTY_CODE;
    }
}
