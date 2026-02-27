package com.tianrenservice.ai_framework_spring.core.pipeline;

import com.tianrenservice.ai_framework_spring.core.entity.BusinessEntity;
import com.tianrenservice.ai_framework_spring.core.entity.BusinessHelper;
import com.tianrenservice.ai_framework_spring.core.exception.DegradeException;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.exception.SkipException;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessDealVO;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 业务门面基类 - 模板方法模式驱动 Pipeline 执行
 *
 * 泛型参数:
 * - V: 输出 DealVO 类型（保留返回类型安全）
 * - T: 业务实体类型（保留 doProcess 参数类型安全）
 * - R: 输入 VO 类型（保留 process 入参类型安全）
 */
@Slf4j
public abstract class BusinessFacade<V extends UserBusinessDealVO<T>, T extends BusinessEntity<?>,
        R extends UserBusinessVO> {

    /**
     * 返回装配线类型编码，框架据此创建 Assembly 实例
     */
    public abstract String getAssemblyTypeCode();

    /**
     * 返回 Helper 实例。默认创建基础 BusinessHelper。
     * 消费方如有自定义 Helper，覆写此方法返回具体子类（Java 支持协变返回类型）。
     */
    public BusinessHelper<R> getBusinessHelper() {
        return new BusinessHelper<>();
    }

    /**
     * 实现业务逻辑
     */
    public abstract void doProcess(T t, R r);

    /**
     * 执行完整 Pipeline：build → ready → process → complete
     */
    public V process(R r) {
        BusinessAssembly a = BusinessAssembly.createForType(getAssemblyTypeCode());
        return process(r, a);
    }

    /**
     * 执行 Pipeline，使用外部提供的 Assembly（供 TestCaseRunner 注入预配置 Assembly）
     */
    @SuppressWarnings("unchecked")
    V process(R r, BusinessAssembly a) {
        try {
            Class<?>[] typeArgs = resolveTypeArguments();
            Class<V> vClazz = (Class<V>) typeArgs[0];
            Class<T> tClazz = (Class<T>) typeArgs[1];

            try {
                a.build(tClazz, vClazz, r);
            } catch (Exception e) {
                log.error("businessFacade process error, userId:{}, {}:{}", r.getUserId(), vClazz.getName(), e.getMessage());
                throw new InterruptException("组合线数据注入中断", e);
            }

            BusinessHelper<R> helper = getBusinessHelper();
            BusinessContext<R> context = BusinessContext.build(r, a);
            helper.setBusinessContext(context);
            T t = BusinessHelper.build(helper, tClazz);

            a.ready(t, r);
            doProcess(t, r);
            V v = t.buildVO(vClazz);
            t.afterProcess();
            a.complete(v, t, r);

            log.debug("businessFacade process end, userId:{}, {}", r.getUserId(), vClazz.getName());
            return v;
        } catch (SkipException e) {
            log.error("businessFacade process skip, userId:{}, message:{}", r.getUserId(), e.getMessage());
            return null;
        } catch (DegradeException e) {
            log.error("businessFacade process degrade, userId:{}, message:{}", r.getUserId(), e.getMessage());
            return null;
        } catch (Exception e) {
            throw new InterruptException("businessFacade process interrupt", e);
        }
    }

    /**
     * 仅构建实体（不执行完整 Pipeline）
     */
    @SuppressWarnings("unchecked")
    public T build(R r) {
        Class<?>[] typeArgs = resolveTypeArguments();
        Class<T> tClazz = (Class<T>) typeArgs[1];
        BusinessAssembly a = BusinessAssembly.createForType(getAssemblyTypeCode());
        BusinessHelper<R> helper = getBusinessHelper();
        BusinessContext<R> context = BusinessContext.build(r, a);
        helper.setBusinessContext(context);
        return BusinessHelper.build(helper, tClazz);
    }

    /**
     * 沿继承链向上查找 BusinessFacade 的实际泛型参数
     * 替代原来假设固定两层继承的脆弱实现
     */
    private Class<?>[] resolveTypeArguments() {
        Type type = this.getClass().getGenericSuperclass();
        while (type != null) {
            if (type instanceof ParameterizedType pt) {
                Type rawType = pt.getRawType();
                if (rawType == BusinessFacade.class) {
                    return Arrays.stream(pt.getActualTypeArguments())
                            .map(t -> (Class<?>) t)
                            .toArray(Class<?>[]::new);
                }
                type = ((Class<?>) rawType).getGenericSuperclass();
            } else if (type instanceof Class<?> cls) {
                type = cls.getGenericSuperclass();
            } else {
                break;
            }
        }
        throw new InterruptException("无法解析 BusinessFacade 泛型参数");
    }
}
