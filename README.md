# Orch-Framework

Orch-Framework 是一个轻量级 Java 业务流程编排框架，解决两个核心问题：一是多步骤流程编排代码与框架耦合问题，通过管道模型，让多步骤业务流程的编排代码与框架解耦；二是涉及外部依赖的业务逻辑难以回归测试的问题，通过环境抽象层录制外部调用并生成测试用例，剥离外部依赖，并支持时间旅行，实现自动化回归测试。核心模块为零依赖纯 Java，可选 Spring Boot Starter 接入。

## 解决什么问题

**业务流程编排的耦合困境** — 企业应用中的复杂业务（订单、审批、结算）通常涉及多步骤串联：构建上下文、执行核心逻辑、落库、清缓存、异步通知……这些步骤的编排逻辑往往与具体框架深度绑定，换一个技术栈就要重写，不同业务之间也难以复用。

**外部依赖让回归测试形同虚设** — 业务逻辑中大量调用数据库、第三方接口、缓存等外部依赖。传统做法是手写 Mock，但 Mock 数据与生产数据的差异会导致测试通过、线上出问题。维护 Mock 的成本也随业务迭代不断增长。

## 核心思路

### 管道编排引擎

将业务流程抽象为 `Facade → Assembly → Entity` 的管道模型：

- **Facade** 是入口，定义 `doProcess()` 实现业务逻辑
- **Assembly** 是编排器，自动驱动 构建 → 就绪 → 处理 → 完成 的生命周期
- **Entity** 是业务载体，用 `@AfterProcess` 注解声明生命周期钩子（落库、清缓存等）

不需要手写 Assembly 子类 — 框架通过 `DynamicAssembly` 自动派生。不需要手写 Helper 子类 — 简单场景直接用默认实现。

**多模块编排** — 对于涉及多步骤的复杂业务，Assembly 支持多种编排方式：

- **声明式编排**：Facade 通过 `requires()` / `provides()` 声明步骤间的依赖关系，`addStep()` 注册步骤后，`run()` 自动进行拓扑排序、循环依赖检测和按序执行
- **命令式编排**：通过 `execute()` 手动控制执行顺序，框架在执行前校验前置依赖是否已发布

步骤间通过 `StepOutputKey<T>` 类型安全键传递数据，Entity 中调用 `require(key)` 即可获取前置步骤的输出，编译期保证类型安全。

### 录制与回放测试

在外部依赖方法上加 `@RecordAndReplay` 注解，框架在 RECORD 模式下捕获真实的入参和返回值，自动生成测试用例。之后切换模式即可回放：

| 模式 | 用途 |
|------|------|
| LIVE | 正常生产运行 |
| RECORD | 录制真实调用，生成测试用例 |
| REPLAY | 回放录制数据，缺失时降级执行 |
| CHECK | 严格回放，缺失则报错 |
| REVIEW | 复盘模式，用于分析 |
| REGENERATE | 重新执行并覆盖旧测试用例 |

### 与 Spring 解耦的架构

核心模块 (`orch-framework-core`) 是零依赖的纯 Java，所有扩展点通过 SPI 接口暴露。Spring Boot Starter 只是一层薄适配器，提供自动装配和 AOP 支持。非 Spring 项目可以直接使用核心模块，手动接入 SPI 实现。

## 快速开始

### 引入依赖

**Spring Boot 项目**（推荐）：

```groovy
implementation 'com.tianrenservice:orch-framework-spring-boot-starter:0.0.1-SNAPSHOT'
```

**纯 Java 项目**：

```groovy
implementation 'com.tianrenservice:orch-framework-core:0.0.1-SNAPSHOT'
```

### 最小示例（4-5 个类）

```java
// 1. 输入 VO
public class OrderVO extends UserBusinessVO { }

// 2. 输出 DealVO
public class OrderDealVO extends UserBusinessDealVO<OrderEntity> { }

// 3. 环境（交互录制载体）
@Component @Scope(SCOPE_PROTOTYPE) @RecordAndReplay
public class OrderEnv extends BusinessEnv { }

// 4. 业务实体
public class OrderEntity extends BusinessEntity<BusinessHelper<OrderVO>> {
    @AfterProcess(Phase.SAVE_DB)
    public void save() { /* 落库逻辑 */ }
}

// 5. 编排入口
@Component
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {
    @Override
    public String getAssemblyTypeCode() { return "order"; }

    @Override
    public void doProcess(OrderEntity entity, OrderVO vo) {
        // 核心业务逻辑
    }
}

// 类型注册（声明为 @Bean 即可自动收集）
@Bean
public BusinessTypeIdentifier orderType() {
    return new SimpleBusinessType("order", "订单处理", OrderVO.class);
}
```

### 多模块编排示例

当业务流程涉及多个步骤（如订单 → 支付 → 发货）时，通过 `StepOutputKey` 声明步骤间依赖，Assembly 自动排序执行：

```java
// 定义步骤输出键（类型安全的公共常量）
public static final StepOutputKey<OrderDealVO> ORDER = StepOutputKey.of("order", OrderDealVO.class);
public static final StepOutputKey<PaymentDealVO> PAYMENT = StepOutputKey.of("payment", PaymentDealVO.class);

// Facade 声明依赖和产出
@Component
public class PaymentFacade extends BusinessFacade<PaymentDealVO, PaymentEntity, PaymentVO> {
    @Override protected Set<StepOutputKey<?>> requires() { return Set.of(ORDER); }
    @Override protected StepOutputKey<PaymentDealVO> provides() { return PAYMENT; }

    @Override
    public void doProcess(PaymentEntity entity, PaymentVO vo) {
        OrderDealVO order = entity.require(ORDER);  // 类型安全地获取前置步骤输出
        // 使用订单信息处理支付...
    }
}

// 注册步骤并执行（注册顺序无关，框架自动拓扑排序）
BusinessAssembly assembly = BusinessAssembly.createForType("order-flow");
assembly.addStep(paymentFacade, paymentVO)
        .addStep(orderFacade, orderVO)
        .run();  // 自动排序: order → payment → finish()
```

## 项目结构

```
orch-framework/
├── orch-framework-core/                  # 纯 Java 核心，零外部依赖
│   └── core/
│       ├── pipeline/                     # 管道编排引擎
│       ├── record/                       # 录制与回放
│       ├── entity/                       # 业务实体基类
│       ├── vo/                           # VO 基类
│       ├── spi/                          # SPI 扩展接口
│       ├── annotation/                   # 生命周期注解
│       ├── testcase/                     # 测试用例管理
│       └── constant/                     # 常量与枚举
└── orch-framework-spring-boot-starter/   # Spring Boot 自动装配 + AOP 适配
    └── autoconfigure/
        ├── OrchFrameworkAutoConfiguration
        ├── RecordAndReplayAspect          # AspectJ 薄适配器
        └── spi/                          # SPI 默认实现（Jackson、Spring）
```

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `orch-framework.record-enabled` | `true` | 录制/回放功能总开关 |
| `orch-framework.aspect-enabled` | `true` | AOP 切面开关 |

## 技术栈

- Java 21
- Gradle 8.14
- 核心模块：纯 Java + SLF4J
- Starter 模块：Spring Boot 4.0.2 + AspectJ + Jackson

## 文档

详细的架构设计与技术文档请参阅 [docs/architecture.md](docs/architecture.md)。

## License

[Apache License 2.0](LICENSE)
