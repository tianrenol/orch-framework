# Orch Framework 全面教程报告

## 一、框架概述

Orch Framework 是一个纯 Java 编写的业务编排框架，旨在解决复杂业务流程的编排、执行和管理问题。该框架采用 Pipeline（流水线）设计模式，通过模板方法模式驱动整个业务流程的执行。框架分为两个核心模块：

- **orch-framework-core**: 纯 Java 核心编排引擎，不依赖任何外部框架
- **orch-framework-spring-boot-starter**: Spring Boot 自动配置模块，提供与 Spring 生态的集成

---

## 二、核心架构

### 2.1 模块划分

```
orch-framework/
├── orch-framework-core/                    # 核心引擎（无外部依赖）
│   └── com.tianrenservice.orch_framework.core/
│       ├── pipeline/                        # 编排核心
│       ├── entity/                          # 实体模型
│       ├── vo/                              # 值对象
│       ├── exception/                       # 异常体系
│       ├── spi/                             # SPI接口
│       ├── record/                          # 录制回放
│       ├── testcase/                        # 测试用例引擎
│       ├── util/                            # 工具类
│       ├── annotation/                     # 注解
│       ├── constant/                        # 常量枚举
│       └── testcase/                        # 测试用例
│
└── orch-framework-spring-boot-starter/      # Spring Boot集成
    └── com.tianrenservice.orch_framework.autoconfigure/
        ├── aspect/                          # AOP切面
        └── spi/                             # Spring SPI实现
```

### 2.2 核心类关系图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BusinessFacade                                  │
│                     (业务门面 - 模板方法模式入口)                             │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  process(R r)                                                        │   │
│  │    → BusinessAssembly.createForType()                               │   │
│  │    → BusinessHelper.build()                                         │   │
│  │    → doProcess()  ←← 业务逻辑实现                                     │   │
│  │    → Entity.buildVO()                                               │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            BusinessAssembly                                   │
│                      (组合线 - Pipeline编排器)                               │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  - businessAssemblyUnits: List<BusinessAssemblyUnit>               │   │
│  │  - outputStore: StepOutputStore                                     │   │
│  │  - pendingSteps: List<StepRegistration>                             │   │
│  │  - mode: BusinessMode                                               │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  execute(Facade, Input)  ←  串联多个Facade执行                        │   │
│  │  addStep(Facade, Input)  ←  注册步骤（不立即执行）                      │   │
│  │  run()                ←  拓扑排序后执行所有步骤                         │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BusinessAssemblyUnit                                 │
│                       (组合线单元 - 单个执行节点)                             │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  - businessContext: BusinessContext<R>                              │   │
│  │  - businessEntity: T                                                │   │
│  │  - businessDealVO: V                                               │   │
│  │  - order: int                                                       │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          BusinessContext                                     │
│                       (业务上下文 - 持有输入和Assembly)                       │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  - businessVo: T (输入VO)                                           │   │
│  │  - assembly: BusinessAssembly                                        │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 三、核心组件详解

### 3.1 BusinessFacade（业务门面）

**职责**: 业务处理的入口类，采用模板方法模式驱动整个 Pipeline。

**泛型参数**:
- `V`: 输出 DealVO 类型（返回结果）
- `T`: 业务实体类型（doProcess 参数）
- `R`: 输入 VO 类型（process 入参）

**核心方法**:

```java
public abstract class BusinessFacade<V extends UserBusinessDealVO<T>, T extends BusinessEntity<?>, R extends UserBusinessVO> {
    
    // 1. 声明装配线类型编码
    public abstract String getAssemblyTypeCode();
    
    // 2. 实现业务逻辑
    public abstract void doProcess(T t, R r);
    
    // 3. 声明依赖的前置步骤输出（可选）
    protected Set<StepOutputKey<?>> requires() {
        return Set.of();
    }
    
    // 4. 声明产出的输出键（可选）
    protected StepOutputKey<?> provides() {
        return null;
    }
    
    // 5. 执行完整Pipeline
    public V process(R r) {
        BusinessAssembly a = BusinessAssembly.createForType(getAssemblyTypeCode());
        V result = process(r, a);
        a.finish();
        return result;
    }
}
```

**执行流程** (`process` 方法):

```
1. createForType(typeCode)     → 创建或获取 BusinessAssembly
2. a.build(tClazz, vClazz, r)  → 构建组合线单元
3. BusinessHelper.build()       → 创建业务实体
4. a.ready(t, r)                → 就绪准备
5. doProcess(t, r)             → 执行核心业务逻辑
6. t.buildVO(vClazz)           → 构建返回VO
7. t.afterProcess()            → 后处理（保存数据库等）
8. a.complete(v, t, r)        → 完成组合线
9. publishOutput()             → 发布步骤输出（如果有）
10. finish()                   → 结束Assembly
```

### 3.2 BusinessAssembly（组合线）

**职责**: Pipeline 编排器，管理整个业务流程的执行。

**核心功能**:

1. **高层编排 API** (`execute` 方法):
   ```java
   // 串联多个 Facade 执行
   public V execute(BusinessFacade<V, T, R> facade, R input)
   ```

2. **步骤注册模式** (`addStep` + `run`):
   ```java
   // 注册步骤（不立即执行）
   public BusinessAssembly addStep(BusinessFacade facade, R input)
   
   // 拓扑排序后执行所有步骤
   public BusinessAssembly run()
   ```

3. **拓扑排序** (`topologicalSort`):
   - 基于 `requires()` / `provides()` 声明
   - 使用 Kahn 算法进行排序
   - 自动检测循环依赖

**业务模式** (`BusinessMode`):

| 模式 | 说明 |
|------|------|
| `LIVE` | 实时模式，正常执行业务 |
| `RECORD` | 录制模式，记录所有交互 |
| `REPLAY` | 回放模式，使用录制数据，未匹配的直接执行 |
| `CHECK` | 检查模式，验证业务逻辑 |
| `REVIEW` | 复盘模式，类似检查但用途不同 |
| `REGENERATE` | 重生成模式，重新执行并更新录制数据 |

### 3.3 StepOutputStore（步骤输出存储）

**职责**: 类型安全的异构容器，用于 Pipeline 步骤间的数据传递。

**核心方法**:
```java
// 发布步骤输出
public <T> void publish(StepOutputKey<T> key, T value)

// 获取步骤输出（必须存在）
public <T> T require(StepOutputKey<T> key)

// 获取步骤输出（可选）
public <T> T get(StepOutputKey<T> key)
```

### 3.4 StepOutputKey（步骤输出键）

**职责**: 类型安全的异构容器键（参考 Effective Java 的 Typesafe Heterogeneous Container 模式）。

**使用方式**:
```java
// 定义输出键
public static final StepOutputKey<OrderDTO> ORDER_RESULT = 
    StepOutputKey.of("order-result", OrderDTO.class);
```

### 3.5 BusinessEntity（业务实体）

**职责**: 承载业务逻辑的核心容器。

**核心功能**:
```java
// 获取前置步骤输出（必须存在）
public <E> E require(StepOutputKey<E> key)

// 获取前置步骤输出（可选）
public <E> E getStepOutput(StepOutputKey<E> key)

// 构建返回VO
public <V extends BusinessDealVO<T>, T extends BusinessEntity<?>> V buildVO(Class<V> clazz)

// 后处理
public void afterProcess() {
    // 1. 执行 @AfterProcess(SAVE_DB)
    // 2. 执行 @AfterProcess(DEL_REDIS)
    // 3. 回退到 Helper.saveDB() / delRedis()
}

// 完成
public void finish() {
    // 1. 执行 @AfterProcess(FINISH)
    // 2. 回退到 Helper.finish()
}
```

### 3.6 BusinessHelper（业务辅助）

**职责**: 管理 Context 和 Env，提供生命周期钩子。

**核心方法**:
```java
// 获取业务环境（支持录制/回放）
public <E extends BusinessEnv> E getBusinessEnv(Class<E> eClass)

// 获取业务上下文
public BusinessContext<R> getBusinessContext()

// 获取当前时间（会被录制）
public long getNowTime()

// 生命周期钩子（可被 @AfterProcess 覆盖）
public void saveDB() {}
public void delRedis() {}
public void finish() {}
```

---

## 四、录制/回放机制

### 4.1 核心概念

框架提供强大的录制/回放能力，用于测试和调试。

**核心组件**:
- `BusinessEnv`: 环境交互管理器
- `InteractionRecord`: 交互记录
- `RecordAndReplayHandler`: 录制/回放处理器
- `@RecordAndReplay`: 录制/回放注解

### 4.2 工作原理

```
┌─────────────────────────────────────────────────────────────────┐
│                        @RecordAndReplay                          │
│              标记需要录制/回放的方法或类                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    RecordAndReplayHandler                        │
│                      录制/回放核心处理器                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  LIVE    → 直接执行                                       │   │
│  │  RECORD  → 执行并保存记录                                │   │
│  │  REPLAY  → 回放记录，未匹配则执行                         │   │
│  │  CHECK   → 验证记录存在并匹配                            │   │
│  │  REVIEW  → 类似CHECK                                     │   │
│  │  REGENERATE → 重新执行并更新记录                         │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BusinessEnv                                │
│                   环境交互管理器                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  records: Map<String, List<InteractionRecord>>        │   │
│  │  covers: Map<String, List<InteractionRecord>>          │   │
│  │  mode: BusinessMode                                     │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 使用方式

```java
public class OrderEnv extends BusinessEnv {
    
    @RecordAndReplay
    public OrderDTO getOrderById(String orderId) {
        // 实际调用外部服务
        return orderService.getOrder(orderId);
    }
    
    @RecordAndReplay("createOrder")
    public OrderDTO createOrder(OrderRequest request) {
        return orderService.create(request);
    }
}
```

---

## 五、SPI 扩展机制

框架通过 SPI（Service Provider Interface）实现可扩展性。

### 5.1 核心 SPI 接口

| 接口 | 职责 | 默认实现 |
|------|------|----------|
| `JsonSerializer` | JSON 序列化 | JacksonJsonSerializer |
| `BeanProvider` | Bean 获取 | SpringBeanProvider |
| `TypeRegistry` | 类型注册 | DefaultTypeRegistry |
| `ExceptionHandler` | 异常处理 | DefaultExceptionHandler |
| `TestCasePersistenceService` | 测试用例持久化 | 用户实现 |

### 5.2 TypeRegistry（类型注册表）

```java
public interface TypeRegistry {
    // 注册业务类型
    void registerBusinessType(BusinessTypeIdentifier type);
    
    // 注册组合线类型
    void registerAssemblyType(AssemblyTypeIdentifier type);
    
    // 根据编码查找业务类型
    BusinessTypeIdentifier resolveBusinessType(String code);
    
    // 根据编码查找组合线类型
    AssemblyTypeIdentifier resolveAssemblyType(String code);
}
```

### 5.3 使用示例

```java
@Bean
public TypeRegistry typeRegistry() {
    DefaultTypeRegistry registry = new DefaultTypeRegistry();
    
    // 注册业务类型
    registry.registerBusiness("order", "订单业务", OrderVO.class);
    registry.registerBusiness("payment", "支付业务", PaymentVO.class);
    
    // 注册组合线类型
    registry.registerAssemblyType(new AssemblyTypeIdentifier(
        "order-flow", OrderFlowAssembly.class
    ));
    
    return registry;
}
```

---

## 六、Spring Boot 集成

### 6.1 自动配置

`OrchFrameworkAutoConfiguration` 自动配置以下组件：

```java
@AutoConfiguration
public class OrchFrameworkAutoConfiguration {
    
    @Bean
    public JsonSerializer jsonSerializer()          // Jackson序列化器
    
    @Bean
    public ExceptionHandler exceptionHandler()      // 默认异常处理
    
    @Bean
    public BeanProvider beanProvider()             // Spring Bean提供器
    
    @Bean
    public TypeRegistry typeRegistry()             // 类型注册表
    
    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public BusinessEnv businessEnv()              // 业务环境
    
    @Bean
    public RecordAndReplayHandler handler()        // 录制/回放处理器
    
    @Bean
    public RecordAndReplayAspect aspect()          // AOP切面
}
```

### 6.2 配置属性

```yaml
orch-framework:
  aspect-enabled: true   # 启用AOP切面（默认true）
```

---

## 七、使用教程

### 7.1 快速开始

**步骤1: 添加依赖**

```xml
<!-- Maven -->
<dependency>
    <groupId>com.tianrenservice</groupId>
    <artifactId>orch-framework-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**步骤2: 定义输入 VO**

```java
public class OrderVO extends UserBusinessVO {
    private String orderId;
    private String productId;
    private Integer quantity;
    
    public OrderVO() {}
    
    public OrderVO(UserBusiness userBusiness) {
        super(userBusiness);
    }
}
```

**步骤3: 定义输出 VO**

```java
public class OrderDealVO extends UserBusinessDealVO<OrderEntity> {
    private String orderId;
    private BigDecimal totalAmount;
    private String status;
    
    public OrderDealVO(UserBusiness userBusiness) {
        super(userBusiness);
    }
    
    @Override
    public OrderDealVO doBuild(OrderEntity entity) {
        this.orderId = entity.getOrderId();
        this.totalAmount = entity.getTotalAmount();
        this.status = entity.getStatus();
        return this;
    }
}
```

**步骤4: 定义业务实体**

```java
public class OrderEntity extends BusinessEntity<OrderHelper<OrderVO>> {
    
    private String orderId;
    private BigDecimal totalAmount;
    private String status;
    
    public OrderEntity(OrderHelper<OrderVO> helper) {
        super(helper);
    }
    
    @AfterProcess(Phase.SAVE_DB)
    public void saveToDatabase() {
        repository.save(this);
    }
    
    @AfterProcess(Phase.DEL_REDIS)
    public void clearCache() {
        redis.delete("order:" + orderId);
    }
}
```

**步骤5: 定义业务辅助类**

```java
public class OrderHelper<R extends UserBusinessVO> extends BusinessHelper<R> {
    
    @RecordAndReplay
    public OrderDTO getOrderFromService(String orderId) {
        return orderService.getOrder(orderId);
    }
}
```

**步骤6: 定义业务门面**

```java
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {
    
    // 定义步骤输出键
    public static final StepOutputKey<OrderDTO> ORDER_DETAIL = 
        StepOutputKey.of("order-detail", OrderDTO.class);
    
    @Override
    public String getAssemblyTypeCode() {
        return "order-flow";
    }
    
    @Override
    public OrderHelper<OrderVO> getBusinessHelper() {
        return new OrderHelper<>();
    }
    
    @Override
    protected Set<StepOutputKey<?>> requires() {
        // 声明依赖，如果这是第一个步骤则返回空
        return Set.of();
    }
    
    @Override
    protected StepOutputKey<?> provides() {
        // 声明产出
        return ORDER_DETAIL;
    }
    
    @Override
    public void doProcess(OrderEntity entity, OrderVO vo) {
        // 执行业务逻辑
        OrderDTO order = entity.getBusinessHelper()
            .getOrderFromService(vo.getOrderId());
        
        entity.setOrderId(order.getId());
        entity.setTotalAmount(order.getAmount());
        entity.setStatus(order.getStatus());
    }
}
```

**步骤7: 执行流程**

```java
@RestController
public class OrderController {
    
    @Autowired
    private OrderFacade orderFacade;
    
    @PostMapping("/order/process")
    public OrderDealVO processOrder(@RequestBody OrderVO orderVO) {
        return orderFacade.process(orderVO);
    }
}
```

### 7.2 高级用法

#### 7.2.1 多步骤编排

```java
public class OrderFlowAssembly extends BusinessAssembly {
    
    @Override
    public String getAssemblyTypeCode() {
        return "order-flow";
    }
}

@Service
public class OrderFlowService {
    
    @Autowired
    private OrderFacade orderFacade;
    
    @Autowired
    private PaymentFacade paymentFacade;
    
    public OrderDealVO processOrder(OrderVO orderVO, PaymentVO paymentVO) {
        OrderFlowAssembly assembly = new OrderFlowAssembly();
        
        // 方式1: 使用 execute 串联
        OrderDealVO orderResult = assembly.execute(orderFacade, orderVO);
        
        // 修改支付输入
        paymentVO.setOrderId(orderResult.getOrderId());
        
        PaymentDealVO paymentResult = assembly.execute(paymentFacade, paymentVO);
        
        assembly.finish();
        
        return orderResult;
    }
    
    // 方式2: 使用 addStep + run 自动排序
    public void processOrderAuto(OrderVO orderVO, PaymentVO paymentVO) {
        OrderFlowAssembly assembly = new OrderFlowAssembly();
        
        assembly.addStep(orderFacade, orderVO)
                .addStep(paymentFacade, paymentVO)
                .run();
    }
}
```

#### 7.2.2 测试用例引擎

```java
@Service
public class TestCaseService {
    
    @Autowired
    private TestCasePersistenceService persistenceService;
    
    @Autowired
    private JsonSerializer jsonSerializer;
    
    @Autowired
    private OrderFacade orderFacade;
    
    public void testOrderFlow() {
        TestCaseEngine engine = new TestCaseEngine(
            orderFacade, 
            persistenceService, 
            jsonSerializer
        );
        
        // 录制测试用例
        TestCaseVO testCase = new TestCaseVO();
        testCase.setName("order-flow-test");
        TestCaseVO recorded = engine.record(testCase);
        
        // 回放测试用例
        TestCaseVO replayed = engine.replay(recorded);
        
        // 检查测试用例
        boolean passed = engine.check(recorded.getId());
    }
}
```

---

## 八、异常处理

### 8.1 异常体系

```
BusinessException (基类)
├── InterruptException    → 中断当前业务流程
├── SkipException        → 跳过当前操作，不中断流程
└── DegradeException     → 触发降级逻辑
```

### 8.2 自定义异常处理

```java
@Component
public class CustomExceptionHandler implements ExceptionHandler {
    
    @Override
    public <V> V onSkip(SkipException e, UserBusinessVO input) {
        log.warn("业务跳过: {}", e.getMessage());
        return null;
    }
    
    @Override
    public <V> V onDegrade(DegradeException e, UserBusinessVO input) {
        log.warn("业务降级: {}", e.getMessage());
        e.degrade();  // 执行降级逻辑
        return null;
    }
    
    @Override
    public <V> V onException(Exception e, UserBusinessVO input) {
        log.error("业务异常: {}", e.getMessage(), e);
        throw new RuntimeException(e);
    }
}
```

---

## 九、最佳实践

### 9.1 设计原则

1. **单一职责**: 每个 Facade 只负责一个业务环节
2. **依赖声明**: 使用 `requires()` / `provides()` 显式声明依赖
3. **状态管理**: 通过 Entity 管理业务状态，通过 Env 管理外部交互
4. **异常处理**: 合理使用 SkipException 和 DegradeException

### 9.2 性能优化

1. **BusinessEnv 作用域**: 使用 Prototype 作用域，确保每个流程独立
2. **异步任务**: 使用 `BusinessFutureTask` 处理耗时操作
3. **录制优化**: 仅对必要的外部调用使用 `@RecordAndReplay`

### 9.3 测试策略

1. **录制模式**: 开发时录制业务交互
2. **回放模式**: 调试时快速复现问题
3. **检查模式**: 自动化测试验证业务逻辑

---

## 十、总结

Orch Framework 是一个设计精良的业务编排框架，提供了：

- **Pipeline 编排**: 基于模板方法模式的流程执行
- **类型安全**: 泛型和 StepOutputKey 确保类型安全
- **录制/回放**: 强大的测试和调试能力
- **SPI 扩展**: 灵活的可扩展机制
- **Spring 集成**: 简洁的 Spring Boot 集成

通过本教程，你应该能够全面理解框架的编排逻辑，并能够熟练使用框架进行业务开发。
