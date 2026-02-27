package com.tianrenservice.ai_framework_spring.core.spi;

import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;

/**
 * 类型注册表契约 - 替代原 AssemblyEnum/BusinessEnum 的静态遍历
 * 业务方在启动时注册自己的业务类型和组合线类型
 */
public interface TypeRegistry {

    /**
     * 注册业务类型
     */
    void registerBusinessType(BusinessTypeIdentifier type);

    /**
     * 注册组合线类型
     */
    void registerAssemblyType(AssemblyTypeIdentifier type);

    /**
     * 根据编码查找业务类型
     */
    BusinessTypeIdentifier resolveBusinessType(String code);

    /**
     * 根据编码查找组合线类型
     */
    AssemblyTypeIdentifier resolveAssemblyType(String code);

    /**
     * 根据业务类型编码查找 VO 类
     */
    Class<? extends UserBusinessVO> resolveVoClass(String typeCode);

    /**
     * 便捷注册：一步注册业务类型
     */
    default void registerBusiness(String code, String description,
                                   Class<? extends UserBusinessVO> voClass) {
        registerBusinessType(new SimpleBusinessType(code, description, voClass));
    }
}
