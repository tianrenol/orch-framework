package com.tianrenservice.orch_framework.core.testcase.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 测试用例 VO - 框架内部定义，替代原 BusinessTestCaseVO (Dubbo API)
 */
@Getter
@Setter
public class TestCaseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String description;
    private String businessType;
    private String businessName;
    private String multiBusinessVO;
    private String multiBusinessEnv;
    private String multiBusinessDealVO;
}
