package com.tianrenservice.orch_framework.autoconfigure.spi;

import com.tianrenservice.orch_framework.core.spi.AssemblyTypeIdentifier;
import com.tianrenservice.orch_framework.core.spi.BusinessTypeIdentifier;
import com.tianrenservice.orch_framework.core.spi.TypeRegistry;
import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TypeRegistry 的内存默认实现
 * 业务方在 Spring 启动时通过此注册表注册自己的类型
 */
@Slf4j
public class DefaultTypeRegistry implements TypeRegistry {

    private final Map<String, BusinessTypeIdentifier> businessTypes = new ConcurrentHashMap<>();
    private final Map<String, AssemblyTypeIdentifier> assemblyTypes = new ConcurrentHashMap<>();

    @Override
    public void registerBusinessType(BusinessTypeIdentifier type) {
        businessTypes.put(type.getCode(), type);
        log.info("注册业务类型: {} ({})", type.getCode(), type.getDescription());
    }

    @Override
    public void registerAssemblyType(AssemblyTypeIdentifier type) {
        assemblyTypes.put(type.getCode(), type);
        log.info("注册组合线类型: {} ({})", type.getCode(), type.getDescription());
    }

    @Override
    public BusinessTypeIdentifier resolveBusinessType(String code) {
        return businessTypes.get(code);
    }

    @Override
    public AssemblyTypeIdentifier resolveAssemblyType(String code) {
        return assemblyTypes.get(code);
    }

    @Override
    public Class<? extends UserBusinessVO> resolveVoClass(String typeCode) {
        BusinessTypeIdentifier type = businessTypes.get(typeCode);
        return type != null ? type.getVoClass() : null;
    }
}
