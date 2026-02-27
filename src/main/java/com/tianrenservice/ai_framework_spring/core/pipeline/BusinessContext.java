package com.tianrenservice.ai_framework_spring.core.pipeline;

import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 业务上下文 - 持有当前业务的输入 VO 和组合线引用
 */
@Getter
@Slf4j
@Builder
public class BusinessContext<T extends UserBusinessVO> {

    private final T businessVo;
    private final BusinessAssembly assembly;

    @SuppressWarnings("unchecked")
    public static <T extends UserBusinessVO> BusinessContext<T> build(T businessVo, BusinessAssembly assembly) {
        return (BusinessContext<T>) BusinessContext.builder()
                .businessVo(businessVo)
                .assembly(Objects.nonNull(assembly) ? assembly : BusinessAssembly.createAssembly(BusinessEmptyAssembly.class))
                .build();
    }

    public String getUserId() {
        return businessVo.getUserId();
    }
}
