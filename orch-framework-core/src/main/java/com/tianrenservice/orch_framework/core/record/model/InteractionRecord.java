package com.tianrenservice.orch_framework.core.record.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 交互记录 - 记录方法调用的输入参数和返回结果
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class InteractionRecord implements Serializable {
    private static final long serialVersionUID = 8955514477564055164L;
    private String methodName;
    private List<Object> arguments;
    private Object result;
}
