# Orch-Framework 架构设计与技术文档

<!-- TOC -->
* [Orch-Framework 架构设计与技术文档](#orch-framework-架构设计与技术文档)
  * [一、整体架构设计](#一整体架构设计)
    * [1.1 架构分层](#11-架构分层)
    * [1.2 整体架构图](#12-整体架构图)
    * [1.3 核心类图](#13-核心类图)
    * [1.4 时序图 - 业务处理流程](#14-时序图---业务处理流程)
    * [1.5 数据流图](#15-数据流图)
    * [1.6 模块依赖关系](#16-模块依赖关系)
  * [二、核心抽象层设计 (core模块)](#二核心抽象层设计-core模块)
    * [2.1 Business 基类体系](#21-business-基类体系)
    * [2.2 VO (Value Object) 体系](#22-vo-value-object-体系)
    * [2.3 BusinessFacade - 模板方法驱动](#23-businessfacade---模板方法驱动)
    * [2.4 BusinessHelper - 协作者模式](#24-businesshelper---协作者模式)
    * [2.5 BusinessEntity - 领域模型](#25-businessentity---领域模型)
    * [2.6 BusinessEnv - 环境抽象与录制回放](#26-businessenv---环境抽象与录制回放)
    * [2.7 BusinessAssembly - 管道编排引擎](#27-businessassembly---管道编排引擎)
  * [三、SPI 扩展点设计](#三spi-扩展点设计)
    * [3.1 SPI 接口总览](#31-spi-接口总览)
    * [3.2 类型注册体系](#32-类型注册体系)
    * [3.3 序列化与Bean提供者](#33-序列化与bean提供者)
  * [四、录制与回放系统](#四录制与回放系统)
    * [4.1 六种业务模式](#41-六种业务模式)
    * [4.2 RecordAndReplay 注解](#42-recordandreplay-注解)
    * [4.3 RecordAndReplayHandler - 核心处理器](#43-recordandreplayhandler---核心处理器)
    * [4.4 交互记录匹配机制](#44-交互记录匹配机制)
  * [五、测试用例管理系统](#五测试用例管理系统)
    * [5.1 TestCaseEngine - 测试引擎](#51-testcaseengine---测试引擎)
    * [5.2 TestCaseRunner - 执行器](#52-testcaserunner---执行器)
    * [5.3 TestCaseComparator - 结果比较器](#53-testcasecomparator---结果比较器)
    * [5.4 测试用例数据模型](#54-测试用例数据模型)
  * [六、管道编排引擎详解](#六管道编排引擎详解)
    * [6.1 BusinessAssemblyUnit - 执行节点](#61-businessassemblyunit---执行节点)
    * [6.2 DynamicAssembly - 动态组合线](#62-dynamicassembly---动态组合线)
    * [6.3 管道执行生命周期](#63-管道执行生命周期)
  * [七、异常体系设计](#七异常体系设计)
    * [7.1 异常类型总览](#71-异常类型总览)
    * [7.2 异常处理策略](#72-异常处理策略)
  * [八、Spring Boot Starter 模块](#八spring-boot-starter-模块)
    * [8.1 自动配置机制](#81-自动配置机制)
    * [8.2 配置属性](#82-配置属性)
    * [8.3 RecordAndReplayAspect - AOP 适配器](#83-recordandreplayaspect---aop-适配器)
    * [8.4 SPI 默认实现](#84-spi-默认实现)
  * [九、设计模式与编程范式](#九设计模式与编程范式)
    * [9.1 使用的设计模式](#91-使用的设计模式)
    * [9.2 泛型编程](#92-泛型编程)
    * [9.3 依赖注入模式](#93-依赖注入模式)
    * [9.4 反射与缓存优化](#94-反射与缓存优化)
    * [9.5 异步任务模型](#95-异步任务模型)
  * [十、快速开始](#十快速开始)
    * [10.1 引入依赖](#101-引入依赖)
    * [10.2 最小接入示例 (4-5个类)](#102-最小接入示例-4-5个类)
    * [10.3 自定义 Helper 扩展示例](#103-自定义-helper-扩展示例)
    * [10.4 自定义 Env 与录制回放示例](#104-自定义-env-与录制回放示例)
    * [10.5 测试用例录制与回放示例](#105-测试用例录制与回放示例)
    * [10.6 自定义 SPI 实现示例](#106-自定义-spi-实现示例)
    * [10.7 高级用法：自定义 Assembly](#107-高级用法自定义-assembly)
  * [十一、最佳实践](#十一最佳实践)
    * [11.1 开发规范](#111-开发规范)
      * [11.1.1 命名规范](#1111-命名规范)
      * [11.1.2 注解使用规范](#1112-注解使用规范)
      * [11.1.3 Env 方法规范](#1113-env-方法规范)
      * [11.1.4 异常处理规范](#1114-异常处理规范)
    * [11.2 性能优化](#112-性能优化)
    * [11.3 测试最佳实践](#113-测试最佳实践)
    * [11.4 故障排查](#114-故障排查)
  * [十二、常见问题 FAQ](#十二常见问题-faq)
    * [12.1 架构相关](#121-架构相关)
    * [12.2 测试相关](#122-测试相关)
    * [12.3 性能相关](#123-性能相关)
    * [12.4 扩展相关](#124-扩展相关)
  * [十三、总结](#十三总结)
    * [13.1 核心优势](#131-核心优势)
    * [13.2 学习路径](#132-学习路径)
    * [13.3 注意事项](#133-注意事项)
<!-- TOC -->

## 一、整体架构设计

### 1.1 架构分层

Orch-Framework 是一个**可复用的业务流程编排框架**，采用核心与Spring解耦的多模块架构，整体分为**两大模块、四层架构**：

```
┌─────────────────────────────────────────────────────┐
│  Consumer Layer (消费方业务代码)                      │  ← Facade + Entity + VO
├─────────────────────────────────────────────────────┤
│  Pipeline Layer (BusinessAssembly)                   │  ← 管道编排层
├─────────────────────────────────────────────────────┤
│  Domain Layer (Entity / Helper / Env)                │  ← 领域模型层
├─────────────────────────────────────────────────────┤
│  Core Infrastructure Layer (SPI / Record / TestCase) │  ← 核心基础设施层
└─────────────────────────────────────────────────────┘
```

**核心设计理念：**
- **框架无关**：核心模块纯 Java 实现，零 Spring/AspectJ 依赖
- **SPI 驱动**：所有扩展点通过接口定义，消费方可自由替换实现
- **录制回放**：内置六种业务模式，支持交互录制、回放、校验、复盘
- **最小侵入**：消费方仅需 4-5 个类即可接入完整生命周期

### 1.2 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       Consumer Business Code (消费方)                     │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  OrderFacade / PaymentFacade / ...                                 │ │
│  │  OrderEntity / PaymentEntity / ...                                 │ │
│  │  OrderVO / OrderDealVO / OrderEnv / ...                            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────────────┐
│              orch-framework-spring-boot-starter (Spring 集成层)          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  OrchFrameworkAutoConfiguration (自动配置)                          │ │
│  │  ├─ DefaultTypeRegistry        ← 类型自动收集与注册                │ │
│  │  ├─ JacksonJsonSerializer      ← JSON 序列化                      │ │
│  │  ├─ SpringBeanProvider         ← Spring 容器适配                   │ │
│  │  └─ RecordAndReplayAspect      ← AOP 薄壳适配器                   │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────────────┐
│                   orch-framework-core (纯 Java 核心层)                   │
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────┐  │
│  │  core.pipeline    │  │  core.record     │  │  core.testcase       │  │
│  │                   │  │                  │  │                      │  │
│  │  BusinessFacade   │  │  @RecordAndReplay│  │  TestCaseEngine      │  │
│  │  BusinessAssembly │  │  RecordHandler   │  │  TestCaseRunner      │  │
│  │  AssemblyUnit     │  │  BusinessEnv     │  │  TestCaseComparator  │  │
│  │  BusinessContext  │  │  InteractionRec  │  │  TestCaseVO          │  │
│  │  DynamicAssembly  │  │                  │  │                      │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────────┘  │
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────┐  │
│  │  core.entity      │  │  core.vo         │  │  core.spi            │  │
│  │                   │  │                  │  │                      │  │
│  │  BusinessEntity   │  │  BusinessVO      │  │  TypeRegistry        │  │
│  │  BusinessHelper   │  │  UserBusinessVO  │  │  JsonSerializer      │  │
│  │  Business         │  │  BusinessDealVO  │  │  BeanProvider        │  │
│  │  UserBusiness     │  │  UserBizDealVO   │  │  BusinessTypeId      │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────────┘  │
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────┐  │
│  │  core.annotation  │  │  core.constant   │  │  core.exception      │  │
│  │  @AfterProcess    │  │  BusinessMode    │  │  BusinessException   │  │
│  │  Phase enum       │  │  (6种模式)       │  │  DegradeException    │  │
│  │                   │  │                  │  │  InterruptException  │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────────┘  │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  core.util                                                        │  │
│  │  CacheInvoke / BeanUtil / ObjectCompareUtil / LazyFutureTask      │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.3 核心类图

```
┌─────────────────────────────────────────────────────────────────┐
│                       Core Type System                           │
└─────────────────────────────────────────────────────────────────┘

Business (UUID标识 - identity)
    │
    └── UserBusiness (用户ID - userId)
            │
            ├── BusinessVO (输入数据, abstract)
            │       │
            │       └── UserBusinessVO
            │               └── 消费方 OrderVO, PaymentVO, ...
            │
            ├── BusinessEntity<O extends BusinessHelper> (业务实体)
            │       └── 消费方 OrderEntity, PaymentEntity, ...
            │
            └── BusinessDealVO<T extends BusinessEntity> (输出数据, abstract)
                    │
                    └── UserBusinessDealVO<T>
                            └── 消费方 OrderDealVO, PaymentDealVO, ...

┌─────────────────────────────────────────────────────────────────┐
│                     Processing Pipeline                          │
└─────────────────────────────────────────────────────────────────┘

BusinessFacade<V, T, R>              (模板方法入口)
    │   V = UserBusinessDealVO       (输出类型)
    │   T = BusinessEntity           (实体类型)
    │   R = UserBusinessVO           (输入类型)
    │
    ├── process(R) : V               (驱动完整生命周期)
    │       │
    │       ├── 1. Assembly.build()        构建管道单元
    │       ├── 2. Helper.build()          构建实体
    │       ├── 3. Assembly.ready()        就绪阶段
    │       ├── 4. doProcess(T, R)         ← 消费方实现点
    │       ├── 5. Entity.buildVO()        构建输出
    │       ├── 6. Entity.afterProcess()   生命周期回调
    │       └── 7. Assembly.complete()     完成阶段
    │
    └── 消费方实现: OrderFacade, PaymentFacade, ...

┌─────────────────────────────────────────────────────────────────┐
│                       Helper / Env System                        │
└─────────────────────────────────────────────────────────────────┘

BusinessHelper<R extends UserBusinessVO>
    ├── businessContext: BusinessContext<R>
    ├── businessEnv: BusinessEnv
    ├── saveDB() / delRedis() / finish()   (可被 @AfterProcess 替代)
    └── static beanProvider: BeanProvider  (框架级配置)

BusinessEnv
    ├── mode: BusinessMode                  (当前运行模式)
    ├── records: Map<String, List<InteractionRecord>>   (录制数据)
    ├── covers: Map<String, List<InteractionRecord>>    (回放数据)
    └── findMatchingRecord() / argsMatch()  (交互匹配)

┌─────────────────────────────────────────────────────────────────┐
│                         SPI Interfaces                           │
└─────────────────────────────────────────────────────────────────┘

<<interface>> TypeRegistry          ← DefaultTypeRegistry (Spring默认)
<<interface>> JsonSerializer        ← JacksonJsonSerializer (Spring默认)
<<interface>> BeanProvider          ← SpringBeanProvider (Spring默认)
<<interface>> BusinessTypeIdentifier ← SimpleBusinessType (record)
<<interface>> AssemblyTypeIdentifier ← 消费方实现
<<interface>> ScopeIdentifier       ← 消费方实现
<<interface>> TestCasePersistenceService ← 消费方实现
```

### 1.4 时序图 - 业务处理流程

**正常模式 (LIVE)**

```
Consumer        Facade          Assembly        Helper          Entity          Env
  │               │               │               │               │               │
  │──process(vo)─>│               │               │               │               │
  │               │──createAssembly()────────────>│               │               │
  │               │               │<──Assembly────│               │               │
  │               │               │               │               │               │
  │               │──a.build(V,T,vo)────────────>│               │               │
  │               │               │──创建Unit─────>│               │               │
  │               │               │<──Unit─────────│               │               │
  │               │               │               │               │               │
  │               │──new Helper()──────────────────────────────>│               │
  │               │──setContext()──────────────────────────────>│               │
  │               │──Helper.build()─────────────────────────────────────────>│   │
  │               │               │               │<──Entity─────│               │
  │               │               │               │               │               │
  │               │──a.ready(entity, vo)─────────>│               │               │
  │               │               │──unit.ready()─>│               │               │
  │               │               │               │               │               │
  │               │──doProcess(entity, vo)────────────────────────────────>│     │
  │               │               │               │               │──getEnv()──>│
  │               │               │               │               │  (业务交互)  │
  │               │               │               │               │<──result────│
  │               │               │               │<──完成─────────│               │
  │               │               │               │               │               │
  │               │──entity.buildVO()──────────────────────────>│               │
  │               │               │               │<──DealVO─────│               │
  │               │               │               │               │               │
  │               │──entity.afterProcess()─────────────────────>│               │
  │               │               │               │  @AfterProcess(SAVE_DB)      │
  │               │               │               │  @AfterProcess(DEL_REDIS)    │
  │               │               │               │               │               │
  │               │──a.complete(dealVO, entity, vo)────────────>│               │
  │               │               │──before()──────│  (测试数据填充)              │
  │               │               │──finish()──────│               │               │
  │               │               │               │──entity.finish()────────────>│
  │               │               │               │──executeFutureTasks()         │
  │               │               │               │               │               │
  │<──dealVO──────│               │               │               │               │
```

**录制模式 (RECORD)**

```
Consumer        Facade          Assembly        Handler         Env
  │               │               │               │               │
  │──process(vo)─>│               │               │               │
  │               │──setRecordMode()────────────>│               │
  │               │               │──loadRecords──────────────>│
  │               │               │               │               │
  │               │  ... (正常处理流程) ...                       │
  │               │               │               │               │
  │               │  [Env方法被 @RecordAndReplay 拦截]           │
  │               │               │──handle()────>│               │
  │               │               │               │──判断mode───>│
  │               │               │               │  RECORD       │
  │               │               │               │──invoke()─────│ 执行原方法
  │               │               │               │──addRecord()─>│ 记录交互
  │               │               │               │<──result──────│
  │               │               │               │               │
  │               │──a.complete()──────────────>│               │
  │               │               │──fillTestCase()  缓存VO/Env/DealVO
  │               │               │──saveTestCase()  持久化
  │               │               │               │               │
  │<──dealVO──────│               │               │               │
```

### 1.5 数据流图

```
┌─────────────────────────────────────────────────────────────────┐
│                          Data Flow                               │
└─────────────────────────────────────────────────────────────────┘

消费方请求
    │
    ├─ userId: String      (用户标识)
    ├─ identity: String    (业务标识)
    └─ 业务参数...          (消费方自定义)
    │
    ▼
┌─────────────────┐
│  UserBusinessVO  │ 输入VO (消费方子类)
│  ├─ userId       │
│  ├─ identity     │
│  └─ businessType │ → BusinessTypeIdentifier
└────────┬────────┘
         │
         ▼
┌─────────────────┐    ┌─────────────────────────────────────────┐
│  BusinessFacade  │───>│  BusinessAssembly (管道编排)              │
│  process(vo)     │    │  ├─ build()     构建执行单元              │
│                  │    │  ├─ ready()     实体就绪                  │
│  doProcess()     │    │  ├─ complete()  流程完成                  │
│  [消费方逻辑]     │    │  └─ 管理测试模式 (RECORD/REPLAY/CHECK)    │
└────────┬────────┘    └─────────────────────────────────────────┘
         │
         ▼
┌─────────────────┐    ┌───────────────────────────────┐
│  BusinessEntity  │───>│  BusinessHelper               │
│  [业务逻辑载体]   │    │  ├─ context: BusinessContext   │
│                  │    │  ├─ env: BusinessEnv           │
│  @AfterProcess   │    │  └─ beanProvider: BeanProvider │
│  (SAVE_DB)       │    └───────────────────────────────┘
│  (DEL_REDIS)     │               │
│  (FINISH)        │               ▼
└────────┬────────┘    ┌───────────────────────────────┐
         │             │  BusinessEnv                    │
         │             │  ├─ mode: BusinessMode          │
         │             │  ├─ @RecordAndReplay 方法        │
         │             │  │   (外部服务调用/缓存查询等)    │
         │             │  ├─ records: 录制数据             │
         │             │  └─ covers: 回放数据              │
         │             └───────────────────────────────┘
         │
         ▼
┌─────────────────────┐
│  UserBusinessDealVO  │ 输出DealVO (消费方子类)
│  ├─ identity         │
│  ├─ scope            │
│  ├─ nowTime          │
│  └─ ALL_TASKS        │ → List<BusinessFutureTask>
└──────────┬──────────┘
           │
           ▼
      异步任务执行
      业务处理完成
```

### 1.6 模块依赖关系

```
┌─────────────────────────────────────────────────────────────────┐
│                      Module Dependencies                         │
└─────────────────────────────────────────────────────────────────┘

orch-framework-core (纯 Java，零框架依赖)
    ├── core.spi               ← 接口契约层 (最底层，无内部依赖)
    │   ├── TypeRegistry
    │   ├── JsonSerializer
    │   ├── BeanProvider
    │   ├── BusinessTypeIdentifier / SimpleBusinessType
    │   ├── AssemblyTypeIdentifier
    │   ├── ScopeIdentifier
    │   └── TestCasePersistenceService
    │
    ├── core.constant          ← BusinessMode 枚举
    ├── core.annotation        ← @AfterProcess
    ├── core.exception         ← 异常体系
    │
    ├── core.vo                ← 数据模型 (依赖 spi)
    │   ├── BusinessVO / UserBusinessVO
    │   └── BusinessDealVO / UserBusinessDealVO
    │
    ├── core.entity            ← 领域模型 (依赖 vo, spi, annotation)
    │   ├── Business / UserBusiness
    │   ├── BusinessHelper
    │   └── BusinessEntity
    │
    ├── core.record            ← 录制回放 (依赖 entity, spi, constant)
    │   ├── @RecordAndReplay
    │   ├── InteractionRecord
    │   ├── BusinessEnv
    │   └── RecordAndReplayHandler
    │
    ├── core.pipeline          ← 管道编排 (依赖 entity, record, vo, spi)
    │   ├── BusinessContext
    │   ├── BusinessAssemblyUnit
    │   ├── BusinessAssembly
    │   ├── DynamicAssembly
    │   ├── BusinessEmptyAssembly
    │   └── BusinessFacade
    │
    ├── core.testcase          ← 测试用例 (依赖 pipeline, record, spi)
    │   ├── TestCaseVO
    │   ├── TestCaseComparator
    │   ├── TestCaseRunner
    │   └── TestCaseEngine
    │
    └── core.util              ← 工具类 (被各包依赖)
        ├── CacheInvoke
        ├── BeanUtil
        ├── ObjectCompareUtil
        ├── LazyFutureTask
        └── BusinessFutureTask

    ↑ (依赖)

orch-framework-spring-boot-starter (Spring Boot 集成)
    ├── autoconfigure
    │   ├── OrchFrameworkAutoConfiguration   ← 自动装配入口
    │   ├── OrchFrameworkProperties          ← 配置属性
    │   └── RecordAndReplayAspect            ← AOP 适配器
    │
    └── spi (默认 SPI 实现)
        ├── DefaultTypeRegistry
        ├── JacksonJsonSerializer
        └── SpringBeanProvider

    ↑ (依赖)

Consumer Application (消费方应用)
    ├── XxxVO extends UserBusinessVO
    ├── XxxDealVO extends UserBusinessDealVO
    ├── XxxEntity extends BusinessEntity
    ├── XxxEnv extends BusinessEnv        (可选)
    ├── XxxFacade extends BusinessFacade
    └── @Bean BusinessTypeIdentifier      (类型注册)
```

## 二、核心抽象层设计 (core模块)

### 2.1 Business 基类体系

Business 基类体系提供了整个框架的根类型，所有领域对象均从此继承：

```
Business                                        根基类
  ├─ identity: String ("business-" + UUID)      唯一标识，贯穿整个流程
  │
  └─ UserBusiness extends Business
       └─ userId: String                        用户维度标识
```

**设计要点：**
- `identity` 格式为 `"business-" + UUID`，作为全局唯一标识，便于链路追踪和调试
- `UserBusiness` 引入用户维度，适用于 C 端业务场景
- 所有 VO、Entity、DealVO 均继承自此体系，保证类型一致性
- **组合优于继承**：通过 `BusinessHelper` 注入依赖，而非深层继承
- **作用域隔离**：每个 Entity 可通过 `ScopeIdentifier` 标识其业务域

### 2.2 VO (Value Object) 体系

VO 体系分为输入和输出两条继承链，承载业务数据流转：

**输入 VO：**
```java
BusinessVO (abstract)
    ├─ identity: String
    ├─ abstract getBusinessType(): BusinessTypeIdentifier
    │
    └─ UserBusinessVO extends BusinessVO
         ├─ userId: String
         └─ static buildEmpty(Class): 反射构造空VO (测试回放用)
```

**输出 DealVO：**
```java
BusinessDealVO<T extends BusinessEntity<?>> (abstract)
    ├─ identity: String
    ├─ scope: ScopeIdentifier              作用域标识
    ├─ nowTime: long                       处理时间戳
    ├─ ALL_TASKS: CopyOnWriteArrayList     异步任务列表
    ├─ build(T entity): 构建输出结果
    ├─ abstract doBuild(T): 消费方实现具体构建逻辑
    ├─ executeAllFutureTask(): 统一执行所有异步任务
    │
    └─ UserBusinessDealVO<T> extends BusinessDealVO<T>
         ├─ userId: String
         └─ static buildEmpty(Class): 特化构造
```

**关键设计：**
- VO 与 DealVO 分离输入输出关注点，职责清晰
- `ALL_TASKS` 收集异步任务，在 `complete()` 阶段统一触发执行
- `buildEmpty()` 支持测试回放时的空对象构造

### 2.3 BusinessFacade - 模板方法驱动

BusinessFacade 是消费方的核心入口，采用**模板方法模式**驱动完整业务生命周期：

```java
public abstract class BusinessFacade<
    V extends UserBusinessDealVO<T>,    // 输出类型
    T extends BusinessEntity<?>,         // 实体类型
    R extends UserBusinessVO>            // 输入类型
{
    // 消费方必须实现
    abstract String getAssemblyTypeCode();     // 组合线类型编码
    abstract void doProcess(T entity, R vo);   // 业务逻辑实现点

    // 框架驱动的完整生命周期
    public V process(R r) {
        // 1. 创建/获取 Assembly
        // 2. Assembly.build() → 构建管道单元
        // 3. 创建 Helper + Context + Entity
        // 4. Assembly.ready() → 实体就绪
        // 5. doProcess(entity, vo) ← 消费方逻辑
        // 6. Entity.buildVO() → 构建输出
        // 7. Entity.afterProcess() → 生命周期回调
        // 8. Assembly.complete() → 完成阶段
        // 9. return dealVO
    }

    // 可覆写的扩展点
    public BusinessHelper<R> getBusinessHelper();  // 自定义 Helper
}
```

**详细流程步骤：**

```
process(R) → createForType() → assembly.build() → getBusinessHelper()
           → BusinessContext.build() → helper.setBusinessContext()
           → BusinessHelper.build() → assembly.ready()
           → doProcess(T, R) → buildVO() → afterProcess() → assembly.complete()
```

1. `createForType(getAssemblyTypeCode())` — 根据类型编码自动创建 Assembly（DynamicAssembly 或已注册类型）
2. `assembly.build(tClazz, vClazz, r)` — 注册装配单元到装配线
3. `getBusinessHelper()` — 获取 Helper 实例（默认 `new BusinessHelper<>()`，消费方可覆写）
4. `BusinessContext.build(r, assembly)` — 创建上下文，绑定输入 VO 和 Assembly 引用
5. `helper.setBusinessContext(context)` — 将上下文注入 Helper
6. `BusinessHelper.build(helper, tClazz)` — 反射构建领域实体
7. `assembly.ready(entity, vo)` — 装配单元就绪
8. `doProcess(entity, vo)` — 执行业务逻辑（消费方实现，VO 直接传入）
9. `entity.buildVO(vClazz)` — 构建输出视图
10. `entity.afterProcess()` — 后处理（扫描 `@AfterProcess(SAVE_DB/DEL_REDIS)` 注解，回退到 Helper）
11. `assembly.complete(dealVO, entity, vo)` — 装配单元完成，调用 `before()` 填充测试数据，调用 `entity.finish()` 完成回调，RECORD 模式下保存测试用例

**异常处理策略：**

```java
try {
    // 正常流程
} catch (SkipException e) {
    // 跳过当前操作，返回 null
} catch (DegradeException e) {
    // 降级处理，返回 null
} catch (Exception e) {
    // 包装为 InterruptException 向上抛出
    throw new InterruptException("businessFacade process interrupt", e);
}
```

**包级私有方法（供框架内部使用）：**

```java
/** 使用外部提供的 Assembly 执行 Pipeline（供 TestCaseRunner 注入预配置 Assembly） */
V process(R r, BusinessAssembly a) { ... }
```

**泛型解析机制：**

Facade 通过 `resolveTypeArguments()` 方法沿继承链向上查找泛型参数类型（V、T、R），自动推断消费方声明的具体类型，无需手动指定 Class 对象。

**设计取舍：** 框架采用 3 个泛型参数（V, T, R），分别约束输出 VO、实体和输入 VO——这是业务逻辑中最核心的类型安全场景。Assembly 和 Helper 则通过 `getAssemblyTypeCode()` 和 `getBusinessHelper()` 方法实现动态绑定，无需泛型约束。这一设计在保留核心类型安全的同时，将消费方所需类控制在 4~5 个。

### 2.4 BusinessHelper - 协作者模式

BusinessHelper 作为 Entity 的"协作者"，管理上下文、环境和依赖访问：

```java
public abstract class BusinessHelper<R extends UserBusinessVO> {
    private BusinessContext<R> businessContext;   // 管道上下文
    private BusinessEnv businessEnv;              // 环境实例

    // 全局框架配置 (静态)
    private static BeanProvider beanProvider;

    // 生命周期钩子 (可被 @AfterProcess 注解替代)
    public void saveDB()   { }    // 数据库保存阶段
    public void delRedis() { }    // 缓存清理阶段
    public void finish()   { }    // 完成阶段

    // 依赖获取
    public <E extends BusinessEnv> E getBusinessEnv(Class<E> clazz);
    public long getNowTime();

    // 实体构造工厂
    public static <T extends BusinessEntity<?>> T build(
        BusinessHelper<?> helper, Class<T> tClass);
}
```

**设计要点：**
- Helper 将"框架关注点"（Context、Env、Bean获取）与"业务关注点"（Entity逻辑）分离
- 简单场景下，消费方可直接使用默认 `BusinessHelper<R>`，无需创建子类
- `beanProvider` 通过静态配置注入，由 Spring 自动配置或手动设置

### 2.5 BusinessEntity - 领域模型

BusinessEntity 是业务逻辑的核心载体，通过 Helper 访问框架资源：

```java
public class BusinessEntity<O extends BusinessHelper<?>> extends UserBusiness {
    private final O businessHelper;    // 关联的 Helper

    // 快捷访问
    public <E extends BusinessEnv> E getEnv(Class<E> clazz);    // 获取 Env
    public <V extends BusinessDealVO<?>> V buildVO(Class<V> c); // 构建输出VO

    // 生命周期回调 (注解优先，降级到 Helper)
    public void afterProcess() {
        // 1. 扫描 @AfterProcess(Phase.SAVE_DB) 注解方法
        // 2. 扫描 @AfterProcess(Phase.DEL_REDIS) 注解方法
        // 3. 若无注解，降级调用 helper.saveDB() / helper.delRedis()
    }

    public void finish() {
        // 1. 扫描 @AfterProcess(Phase.FINISH) 注解方法
        // 2. 若无注解，降级调用 helper.finish()
    }
}
```

**@AfterProcess 注解驱动的生命周期：**

```java
// 消费方在 Entity 上直接声明生命周期方法
public class OrderEntity extends BusinessEntity<BusinessHelper<OrderVO>> {

    @AfterProcess(Phase.SAVE_DB)
    public void saveOrder() {
        // 保存订单到数据库
    }

    @AfterProcess(Phase.DEL_REDIS)
    public void clearCache() {
        // 清除相关缓存
    }

    @AfterProcess(Phase.FINISH)
    public void sendNotification() {
        // 发送通知
    }
}
```

### 2.6 BusinessEnv - 环境抽象与录制回放

BusinessEnv 是录制回放系统的核心载体，管理交互记录和模式切换：

**核心创新：时间旅行测试 (Time Travel Testing)**

```java
public class BusinessEnv {
    private final long nowTime = System.currentTimeMillis();

    @RecordAndReplay
    public long getNowTime() {
        return getRealNowTime();
    }
}
```

> **注意：** `BusinessEnv` 是纯 POJO，不含任何 Spring 注解。Starter 模块的 `OrchFrameworkAutoConfiguration` 将其注册为原型作用域 Bean（`@Scope(SCOPE_PROTOTYPE)`），消费方子类可自行添加 `@Component` + `@Scope(SCOPE_PROTOTYPE)` 注解。

**核心字段与方法：**

```java
public class BusinessEnv {
    private long nowTime;                                      // 固定时间戳
    private BusinessMode mode = BusinessMode.LIVE;            // 运行模式
    private Map<String, List<InteractionRecord>> records;     // 新增记录 (录制)
    private Map<String, List<InteractionRecord>> covers;      // 覆盖数据 (回放)
    private String testCaseName;

    // 模式切换
    public void setTestMode(BusinessMode mode, String name,
                            Map<String, List<InteractionRecord>> covers);

    // 录制
    public void addRecord(String methodName, InteractionRecord record);

    // 回放匹配
    public InteractionRecord findMatchingRecord(
        List<InteractionRecord> records, List<Object> currentArgs);

    // 参数比较
    public boolean argsMatch(List<Object> recordArgs, List<Object> currentArgs);
}
```

**消费方 Env 示例：**

消费方通过继承 BusinessEnv 并标注 `@RecordAndReplay`，所有公开方法的调用都会被自动录制/回放：

```java
@Component
@Scope(SCOPE_PROTOTYPE)
@RecordAndReplay
public class OrderEnv extends BusinessEnv {

    @Autowired
    private OrderService orderService;

    // 此方法的调用和返回值会被自动录制
    public OrderInfo queryOrder(String orderId) {
        return orderService.getById(orderId);
    }
}
```

### 2.7 BusinessAssembly - 管道编排引擎

BusinessAssembly 是管道编排的核心，管理执行单元链和测试模式：

```java
public abstract class BusinessAssembly {
    // 执行单元链
    private List<BusinessAssemblyUnit<?, ?, ?>> businessAssemblyUnits;
    private BusinessAssemblyUnit<?, ?, ?> currentUnit;

    // 测试模式数据
    private BusinessMode mode = BusinessMode.LIVE;
    private String testCaseName;
    private TestCaseVO testCaseVO;
    private Map<String, BusinessVO> multiBusinessVO;
    private Map<String, Map<String, List<InteractionRecord>>> multiBusinessEnv;
    private Map<String, BusinessDealVO<?>> multiBusinessDealVO;

    // SPI 依赖 (静态注入)
    private static TypeRegistry typeRegistry;
    private static JsonSerializer jsonSerializer;

    // 消费方需实现
    abstract String getAssemblyTypeCode();

    // 生命周期方法
    public void build(Class<V>, Class<T>, UserBusinessVO);   // 构建单元
    public void ready(BusinessEntity<?>, UserBusinessVO);     // 就绪
    public void complete(BusinessDealVO<?>, BusinessEntity<?>, UserBusinessVO);

    // 测试模式切换
    public void setRecordMode(String testCaseName);
    public void setReplayMode(TestCaseVO testCase);
    public void setCheckMode(TestCaseVO testCase);
    public void setReviewMode(TestCaseVO testCase);
    public void setRegenerateMode(TestCaseVO testCase);

    // 测试用例管理
    public TestCaseVO generateTestCase(BusinessVO vo);
    public void saveTestCase(BusinessVO vo);

    // 工厂方法
    public static BusinessAssembly createForType(String typeCode);
}
```

## 三、SPI 扩展点设计

### 3.1 SPI 接口总览

框架通过 SPI 接口定义所有可替换的扩展点，实现核心与实现的解耦：

| 接口 | 职责 | 默认实现 (Spring Starter) | 消费方覆写方式 |
|------|------|---------------------------|---------------|
| `TypeRegistry` | 类型注册与解析 | `DefaultTypeRegistry` | `@Bean TypeRegistry` |
| `JsonSerializer` | JSON 序列化/反序列化 | `JacksonJsonSerializer` | `@Bean JsonSerializer` |
| `BeanProvider` | Bean 依赖查找 | `SpringBeanProvider` | `@Bean BeanProvider` |
| `BusinessTypeIdentifier` | 业务类型声明 | `SimpleBusinessType` (record) | 声明 `@Bean` 即自动收集 |
| `AssemblyTypeIdentifier` | 组合线类型声明 | — | 声明 `@Bean` 即自动收集 |
| `ScopeIdentifier` | 作用域标识 | — | 消费方实现 |
| `TestCasePersistenceService` | 测试用例持久化 | — | 消费方实现 |

### 3.2 类型注册体系

**BusinessTypeIdentifier** - 业务类型标识：

```java
public interface BusinessTypeIdentifier {
    String getCode();                              // 类型编码
    String getDescription();                       // 描述
    Class<? extends UserBusinessVO> getVoClass();  // 关联的 VO 类型
}

// 简化实现 (Java Record)
public record SimpleBusinessType(
    String code,
    String description,
    Class<? extends UserBusinessVO> voClass
) implements BusinessTypeIdentifier { }
```

**TypeRegistry** - 类型注册表：

```java
public interface TypeRegistry {
    void registerBusinessType(BusinessTypeIdentifier type);
    void registerAssemblyType(AssemblyTypeIdentifier type);
    BusinessTypeIdentifier resolveBusinessType(String code);
    AssemblyTypeIdentifier resolveAssemblyType(String code);
    Class<? extends UserBusinessVO> resolveVoClass(String typeCode);

    // 便捷注册方法（默认实现）
    default void registerBusiness(String code, String description,
                                  Class<? extends UserBusinessVO> voClass) {
        registerBusinessType(new SimpleBusinessType(code, description, voClass));
    }
}
```

**自动收集机制：** Spring 自动配置通过 `ObjectProvider<List<BusinessTypeIdentifier>>` 自动收集所有声明为 `@Bean` 的类型标识，无需手动注册。

### 3.3 序列化与Bean提供者

**JsonSerializer：**

```java
public interface JsonSerializer {
    String toJson(Object object);
    <T> T fromJson(String json, Class<T> clazz);
    Map<String, Object> toMap(String json);
}
```

**BeanProvider：**

```java
public interface BeanProvider {
    <T> T getBean(Class<T> clazz);
}
```

## 四、录制与回放系统

### 4.1 六种业务模式

框架内置六种 `BusinessMode`，控制整体运行行为：

| 模式 | 说明 | RecordAndReplayHandler 行为 |
|------|------|---------------------------|
| `LIVE` | 正常生产模式 | 直接调用原方法 |
| `RECORD` | 录制模式 | 调用原方法 + 记录交互到 records |
| `REPLAY` | 回放模式 | 优先从 covers 查找匹配记录，未匹配则执行原方法 |
| `CHECK` | 校验模式 | 从 covers 查找匹配记录，未找到则抛异常 |
| `REVIEW` | 复盘模式 | 同 CHECK，用于人工复盘验证 |
| `REGENERATE` | 重生成模式 | 同 CHECK，用于重新生成测试快照 |

### 4.2 RecordAndReplay 注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordAndReplay {
    String value() default "";    // 方法标识 (默认使用方法名)
    String mark() default "";      // 参数标识方法名 (用于参数标准化)
}
```

**使用方式：**
- **类级别标注**：类上所有公开方法均参与录制回放
- **方法级别标注**：仅标注的方法参与录制回放
- `value()` 自定义方法标识，解决方法重载的唯一性问题
- **`value="ignore"`**：跳过录制/回放，直接执行原方法
- `mark()` 指定参数标准化方法，用于复杂参数的比较

### 4.3 RecordAndReplayHandler - 核心处理器

RecordAndReplayHandler 是纯 Java 实现的录制回放处理器，不依赖任何 AOP 框架：

```java
public class RecordAndReplayHandler {
    @Delegate
    private final CacheInvoke cacheInvoke = new CacheInvoke(true);  // Lombok委托，复用反射缓存
    private final JsonSerializer jsonSerializer;

    /**
     * 核心处理方法 - 根据 BusinessMode 决定执行策略
     */
    public Object handle(
        String methodName,           // 方法名
        Class<?> returnType,         // 返回类型
        RecordAndReplay methodAnno,  // 方法级注解
        RecordAndReplay classAnno,   // 类级注解
        Object target,               // 目标对象 (必须是 BusinessEnv 子类)
        Object[] args,               // 方法参数
        MethodInvoker invoker        // 原方法调用回调
    ) throws Throwable;

    // 函数式接口 - 解耦 AOP 框架
    @FunctionalInterface
    public interface MethodInvoker {
        Object invoke() throws Throwable;
    }
}
```

**切面逻辑决策流程：**

```
┌─────────────┐
│ 方法被调用   │
└──────┬──────┘
       │
       ▼
┌──────────────┐    ┌──────────────────┐
│ key="ignore"?│─是─>│ 直接执行并返回    │
└──────┬───────┘    └──────────────────┘
       │否
       ▼
┌─────────────┐     ┌──────────────────┐
│ LIVE模式?   │──是─>│ 直接执行并返回    │
└──────┬──────┘     └──────────────────┘
       │否
       ▼
┌─────────────┐     ┌──────────────────┐
│ RECORD模式? │──是─>│ 执行方法          │
└──────┬──────┘     │ 记录交互到records │
       │否          └──────────────────┘
       ▼
┌─────────────┐     ┌──────────────────┐
│ REPLAY模式? │──是─>│ 查找covers记录    │
└──────┬──────┘     │ 找到→返回记录结果  │
       │否          │ 未找到→直接执行    │
       ▼            └──────────────────┘
┌──────────────┐    ┌──────────────────┐
│CHECK/REVIEW/ │─是─>│ 查找covers记录    │
│REGENERATE?   │    │ 找到→返回记录结果  │
└──────────────┘    │ 未找到→抛异常     │
                    └──────────────────┘
```

**参数标准化 (getDefaultMark)：**

```java
// 复杂对象转为 Map 进行比对
public Object getDefaultMark(Object o) {
    if (o == null || BeanUtil.isPrimitive(o.getClass())
        || o instanceof String || o instanceof Collection) {
        return o;
    }
    return jsonSerializer.toMap(jsonSerializer.toJson(o));
}
```

**Handler/Adapter 模式：** Handler 持有所有业务逻辑，Spring 的 `RecordAndReplayAspect` 仅是一层薄壳适配器，将 `ProceedingJoinPoint::proceed` 转换为 `MethodInvoker`。非 Spring 环境可直接调用 Handler。

### 4.4 交互记录匹配机制

```java
public class InteractionRecord implements Serializable {
    private String methodName;        // 方法标识
    private List<Object> arguments;   // 参数列表
    private Object result;            // 返回值
}
```

**存储结构示例：**

```
BusinessEnv.records: Map<String, List<InteractionRecord>>
    │
    ├── "getNowTime" → [{methodName, args=[], result=1700000000000}]
    ├── "queryUser"  → [{methodName, args=[{userId: "123"}], result={...}}]
    └── "getConfig"  → [{methodName, args=["key1"], result="value1"},
                         {methodName, args=["key2"], result="value2"}]
```

**匹配流程：**
1. 根据 `methodName` 从 covers 中获取候选记录列表
2. 遍历候选记录，逐个比较 `arguments`
3. 参数比较支持：基本类型、String、Collection、Map、自定义对象（JSON 转换）
4. 找到匹配记录后返回存储的 `result`，并进行类型转换（`doCastResult`）
5. REPLAY 模式下未匹配则回退执行原方法；CHECK/REVIEW/REGENERATE 模式下未匹配则抛异常

## 五、测试用例管理系统

### 5.1 TestCaseEngine - 测试引擎

TestCaseEngine 是测试用例管理的高层 API，提供五种操作：

```java
public class TestCaseEngine {
    private final TestCaseRunner runner;
    private final TestCaseComparator comparator;
    private final TestCasePersistenceService persistenceService;

    // 录制：执行业务流程并保存交互快照
    public TestCaseVO record(TestCaseVO testCase);

    // 校验：加载测试用例，回放执行，比较结果一致性
    public boolean check(Integer testCaseId);

    // 复盘：使用提供的测试用例数据进行验证
    public boolean review(TestCaseVO testCase);

    // 重播：执行测试用例并返回结果快照（不比较）
    public TestCaseVO replay(TestCaseVO testCase);

    // 重生成：基于已有用例重新录制，更新存储
    public TestCaseVO regenerate(Integer id, TestCaseVO testCase);
}
```

### 5.2 TestCaseRunner - 执行器

```java
public class TestCaseRunner {
    private final BusinessFacade<?, ?, ?> facade;
    private final TestCasePersistenceService persistenceService;

    // 按指定模式执行测试用例
    public BusinessAssembly run(BusinessMode mode, TestCaseVO testCaseVO);

    // 从 Assembly 中提取第一个 BusinessVO
    public BusinessVO extractFirstBusinessVO(BusinessAssembly assembly);

    // 生成测试快照
    public TestCaseVO generateSnapshot(BusinessAssembly assembly);
}
```

### 5.3 TestCaseComparator - 结果比较器

```java
public class TestCaseComparator {
    private final JsonSerializer jsonSerializer;
    private final ObjectCompareUtil objectCompareUtil;

    /**
     * 比较两个测试用例的一致性
     * - 反序列化 JSON 数据
     * - 移除 identity 字段 (每次 UUID 不同)
     * - 递归深度比较所有字段
     * - 不一致字段记录到 inconsistentMap
     */
    public boolean equalsCase(TestCaseVO checkCase, TestCaseVO resultCase,
                              Map<String, String> inconsistentMap);
}
```

### 5.4 测试用例数据模型

```java
public class TestCaseVO implements Serializable {
    private Integer id;
    private String name;                // 用例名称
    private String description;         // 用例描述
    private String businessType;        // 业务类型编码
    private String businessName;        // 业务名称
    private String multiBusinessVO;     // JSON: 多业务输入 VO 快照
    private String multiBusinessEnv;    // JSON: 多业务交互记录快照
    private String multiBusinessDealVO; // JSON: 多业务输出 DealVO 快照
}
```

**数据模型设计：** TestCaseVO 将三类数据（输入、交互、输出）以 JSON 字符串形式存储，支持跨版本兼容和灵活的持久化方案。

**三个 JSON 字段的含义：**

- **multiBusinessVO**: 每个装配单元的输入数据快照（`Map<markName, BusinessVO>`）
- **multiBusinessEnv**: 每个装配单元的外部交互记录（`Map<markName, Map<key, List<InteractionRecord>>>`），用于回放
- **multiBusinessDealVO**: 每个装配单元的输出结果（`Map<markName, BusinessDealVO>`），用于比对

**典型使用流程：**

```
1. record()      → 录制生产环境的真实交互
2. check()       → 验证业务逻辑的行为一致性
3. replay()      → 本地调试，无需外部依赖
4. regenerate()  → 业务逻辑调整后更新用例
```

## 六、管道编排引擎详解

### 6.1 BusinessAssemblyUnit - 执行节点

每个 AssemblyUnit 代表管道中的一个执行节点：

```java
public class BusinessAssemblyUnit<V, T, R> {
    private final BusinessContext<R> businessContext;   // 上下文
    private T businessEntity;                           // 实体 (延迟初始化)
    private V businessDealVO;                           // 输出 (延迟初始化)
    private final int order;                            // 执行顺序

    // 生命周期
    public void ready(BusinessEntity<?> t);             // 初始化实体
    public void complete(UserBusinessDealVO<?> v);      // 设置输出
    public boolean isReady() / isComplete();            // 状态查询

    // 唯一标识
    public String getMarkName();    // "assemblyTypeCode-businessTypeCode-order"
                                    // 如: "order-flow-order-1"
}
```

**标记名规则：** `assemblyTypeCode-businessTypeCode-order`，用于测试用例中标识每个装配单元的数据。

### 6.2 DynamicAssembly - 动态组合线

```java
public class DynamicAssembly extends BusinessAssembly {
    private final String typeCode;

    @Override
    public String getAssemblyTypeCode() { return typeCode; }
}
```

**设计意义：** 当 TypeRegistry 中没有注册对应的 Assembly 子类时，框架自动创建 `DynamicAssembly`。消费方无需为每个业务流程编写空壳 Assembly 类，减少样板代码。

### 6.3 管道执行生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                   Pipeline Lifecycle                              │
└─────────────────────────────────────────────────────────────────┘

         ┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐
process: │build │───>│ready │───>│doProc│───>│after │───>│compl │
         │      │    │      │    │      │    │Proc  │    │ete   │
         └──────┘    └──────┘    └──────┘    └──────┘    └──────┘
            │           │           │           │           │
            ▼           ▼           ▼           ▼           ▼
       创建Unit     实体初始化   消费方逻辑   @AfterProc  填充测试数据
       创建Context  Entity就绪  Entity交互   saveDB()    执行finish()
       创建Helper               Env录制     delRedis()  异步任务执行

 ═══════════════════════════════════════════════════════════════════
 RECORD模式额外：                                    saveTestCase()
 REPLAY模式额外：loadTestCase() → Env从covers回放
 CHECK模式额外：loadTestCase() → Env严格匹配 → 比较结果
```

## 七、异常体系设计

### 7.1 异常类型总览

```
RuntimeException
    └── BusinessException (基础异常, 支持格式化参数)
            │
            ├── DegradeException    (降级异常)
            │   └─ degrade(): 执行降级 Runnable
            │
            ├── InterruptException  (中断异常)
            │   └─ getInterruptWarp(cause): 递归获取最内层中断异常
            │
            └── SkipException       (跳过异常)
                └─ 跳过当前操作，不中断整体流程
```

### 7.2 异常处理策略

| 异常类型 | 触发场景 | 处理策略 |
|---------|---------|---------|
| `BusinessException` | 通用业务错误 | 支持 `String.format` 风格的参数化消息 |
| `DegradeException` | 需要降级处理 | 携带 `Runnable` 回调，调用 `degrade()` 执行降级逻辑 |
| `InterruptException` | 需要中断流程 | 通过 `getInterruptWarp()` 递归查找最内层异常，支持异常链追踪 |
| `SkipException` | 跳过当前步骤 | 不影响后续管道节点执行，用于可选步骤的优雅跳过 |

**InterruptException 特殊能力：**

```java
// 递归解包嵌套的 InterruptException
public static Throwable getInterruptWarp(Throwable cause) {
    if (Objects.isNull(cause)) return null;
    if (cause.getCause() instanceof InterruptException) return cause.getCause();
    return getInterruptWarp(cause.getCause());
}

// 从异常链中提取中断消息
public static String getInterruptMessage(Throwable cause, String defaultMsg) {
    return Optional.ofNullable(getInterruptWarp(cause))
            .map(Throwable::getMessage).orElse(defaultMsg);
}
```

**DegradeException 降级机制：**

```java
public class DegradeException extends BusinessException {
    Runnable degrade;  // 降级处理器

    public DegradeException(String message, Runnable degrade) {
        super(message);
        this.degrade = degrade;
    }

    public void degrade() {
        log.warn("Degrade logic executed for exception: {}", getMessage());
        if (degrade != null) {
            try {
                degrade.run();
            } catch (Exception e) {
                throw new InterruptException("降级处理失败", e);
            }
        }
    }
}
```

**在 Facade 中的处理方式：**

| 异常类型 | Facade 处理 |
|---------|-----------|
| `InterruptException` | 向上抛出，终止流程 |
| `SkipException` | 返回 null，继续后续流程 |
| `DegradeException` | 返回 null（降级逻辑由调用方执行） |

## 八、Spring Boot Starter 模块

### 8.1 自动配置机制

`OrchFrameworkAutoConfiguration` 是 Spring Boot 自动配置的入口，通过 `@ConditionalOnMissingBean` 提供默认实现，消费方可通过声明同类型 `@Bean` 覆写任何默认行为：

```java
@AutoConfiguration
@EnableConfigurationProperties(OrchFrameworkProperties.class)
public class OrchFrameworkAutoConfiguration {

    @Bean @ConditionalOnMissingBean
    public JsonSerializer jsonSerializer() {
        return new JacksonJsonSerializer();
    }

    @Bean @ConditionalOnMissingBean
    public BeanProvider beanProvider(ApplicationContext context) {
        SpringBeanProvider provider = new SpringBeanProvider(context);
        BusinessHelper.configureBeanProvider(provider);  // 初始化框架
        return provider;
    }

    @Bean @ConditionalOnMissingBean
    public TypeRegistry typeRegistry(
            JsonSerializer jsonSerializer,
            ObjectProvider<List<BusinessTypeIdentifier>> businessTypes,
            ObjectProvider<List<AssemblyTypeIdentifier>> assemblyTypes) {
        DefaultTypeRegistry registry = new DefaultTypeRegistry();
        // 自动收集所有 BusinessTypeIdentifier Bean 并注册
        businessTypes.ifAvailable(types -> types.forEach(registry::registerBusinessType));
        // 自动收集所有 AssemblyTypeIdentifier Bean 并注册
        assemblyTypes.ifAvailable(types -> types.forEach(registry::registerAssemblyType));
        BusinessAssembly.configure(registry, jsonSerializer);
        return registry;
    }

    @Bean @Scope(SCOPE_PROTOTYPE) @ConditionalOnMissingBean
    public BusinessEnv businessEnv() { return new BusinessEnv(); }

    @Bean @ConditionalOnMissingBean
    public RecordAndReplayHandler recordAndReplayHandler(JsonSerializer s) {
        return new RecordAndReplayHandler(s);
    }

    @Bean
    @ConditionalOnProperty(prefix = "orch-framework", name = "aspect-enabled",
                           havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public RecordAndReplayAspect recordAndReplayAspect(RecordAndReplayHandler handler) {
        return new RecordAndReplayAspect(handler);
    }
}
```

**Spring Boot 启动时序：**

1. 加载 OrchFrameworkAutoConfiguration
       │
2. ├── 创建 JsonSerializer Bean (JacksonJsonSerializer)
   │
3. ├── 创建 BeanProvider Bean (SpringBeanProvider)
   │       └── BusinessHelper.configureBeanProvider(provider)  ← 全局配置
   │
4. ├── 创建 TypeRegistry Bean (DefaultTypeRegistry)
   │       ├── ObjectProvider 自动收集所有 BusinessTypeIdentifier @Bean
   │       ├── ObjectProvider 自动收集所有 AssemblyTypeIdentifier @Bean
   │       └── BusinessAssembly.configure(typeRegistry, jsonSerializer)  ← 全局配置
   │
5. ├── 创建 BusinessEnv Bean (@Scope PROTOTYPE)
   │
6. ├── 创建 RecordAndReplayHandler Bean
   │
7. └── 创建 RecordAndReplayAspect Bean (条件: aspect-enabled=true)
```

**Bean 注册清单：**

| Bean | 默认实现 | 条件 |
|------|---------|------|
| `jsonSerializer` | `JacksonJsonSerializer` | `@ConditionalOnMissingBean` |
| `beanProvider` | `SpringBeanProvider` | `@ConditionalOnMissingBean` |
| `typeRegistry` | `DefaultTypeRegistry` | `@ConditionalOnMissingBean` |
| `businessEnv` | `BusinessEnv` | `@ConditionalOnMissingBean` + `@Scope(PROTOTYPE)` |
| `recordAndReplayHandler` | `RecordAndReplayHandler` | `@ConditionalOnMissingBean` |
| `recordAndReplayAspect` | `RecordAndReplayAspect` | `@ConditionalOnMissingBean` + `aspect-enabled=true` |

### 8.2 配置属性

```properties
# 配置前缀: orch-framework
orch-framework.record-enabled=true    # 录制/回放功能总开关
orch-framework.aspect-enabled=true    # AOP 切面开关
```

### 8.3 RecordAndReplayAspect - AOP 适配器

```java
@Aspect
public class RecordAndReplayAspect {
    private final RecordAndReplayHandler handler;

    @Pointcut("@within(RecordAndReplay) || @annotation(RecordAndReplay)")
    public void recordAndReplayPointcut() { }

    @Around("recordAndReplayPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) {
        // 1. 提取方法名、返回类型、注解信息
        // 2. joinPoint::proceed 作为 MethodInvoker 回调
        // 3. 委托给 RecordAndReplayHandler.handle()
    }
}
```

**薄壳设计：** Aspect 仅做 AspectJ → Handler 的参数适配，所有业务逻辑在 Handler 中。非 Spring 用户可直接调用 Handler 实现相同功能。

### 8.4 SPI 默认实现

**DefaultTypeRegistry：**
- 使用 `ConcurrentHashMap` 存储，线程安全
- 日志记录每次注册事件

**JacksonJsonSerializer：**
- 基于 Jackson ObjectMapper
- 配置 `FAIL_ON_UNKNOWN_PROPERTIES = false`（兼容字段变更）
- 配置 `FAIL_ON_EMPTY_BEANS = false`（支持空对象序列化）

**SpringBeanProvider：**
- 委托 `ApplicationContext.getBean(Class)` 实现

## 九、设计模式与编程范式

### 9.1 使用的设计模式

| 设计模式 | 应用位置 | 说明 |
|---------|---------|------|
| **模板方法** | `BusinessFacade.process()` | 定义 build → ready → process → complete 流程骨架，`doProcess()` 为消费方实现点 |
| **门面模式** | `BusinessFacade` | 封装复杂的 Pipeline 执行流程，对外提供简单的 `process()` 入口 |
| **组合模式** | `BusinessAssembly` + `BusinessAssemblyUnit` | 装配线由多个装配单元组合而成 |
| **策略模式** | `BusinessMode` + `RecordAndReplayHandler` | 六种模式选择不同的录制/回放策略 |
| **适配器模式** | `RecordAndReplayAspect` → `Handler` | AspectJ 到纯 Java Handler 的适配；`SpringBeanProvider` 到框架接口的适配 |
| **工厂模式** | `BusinessAssembly.createForType()` | 根据类型编码动态创建 Assembly 实例 |
| **建造者模式** | `BusinessContext @Builder` | Lombok Builder 构造上下文对象 |
| **SPI 模式** | `core.spi` 包全部接口 | 7 个核心接口，默认实现可被消费方覆写 |
| **原型模式** | `BusinessEnv` (Starter 注册为原型 Bean) | 每次获取都是新实例，保证环境隔离 |
| **代理/装饰** | `CacheInvoke`、`RecordAndReplayHandler` | 方法调用缓存代理；录制回放代理 |
| **观察者模式** | `@AfterProcess` 注解、`BusinessFutureTask` | 生命周期事件的声明式钩子；自动注册到 DealVO 的任务列表 |
| **懒加载** | `LazyFutureTask` | 延迟执行，首次 `get()` 时才触发 `run()` |
| **空对象** | `BusinessEmptyAssembly` | 占位 Assembly，避免 null 判断 |
| **Handler/Adapter** | `RecordAndReplayHandler` + `Aspect` | 核心逻辑与 AOP 框架解耦，Handler 纯 Java，Aspect 为薄壳适配器 |

### 9.2 泛型编程

框架广泛使用泛型确保类型安全：

```java
// Facade 三泛型约束完整的输入输出类型链
BusinessFacade<V extends UserBusinessDealVO<T>,
               T extends BusinessEntity<?>,
               R extends UserBusinessVO>

// Entity 泛型约束关联的 Helper 类型
BusinessEntity<O extends BusinessHelper<?>>

// Helper 泛型约束输入 VO 类型
BusinessHelper<R extends UserBusinessVO>

// DealVO 泛型约束关联的 Entity 类型
BusinessDealVO<T extends BusinessEntity<?>>
```

**泛型解析：** `BusinessFacade.resolveTypeArguments()` 通过反射沿继承链向上查找 `ParameterizedType`，自动推断消费方声明的具体泛型参数类型。

### 9.3 依赖注入模式

**框架采用双重依赖注入策略：**

1. **Spring DI**: 自动配置层通过 `@Bean` 注入 SPI 实现
2. **静态配置**: 核心层通过静态方法配置 SPI（`BusinessHelper.configureBeanProvider()`、`BusinessAssembly.configure()`）

```java
// 自动配置时初始化
@Bean
public BeanProvider beanProvider(ApplicationContext context) {
    SpringBeanProvider provider = new SpringBeanProvider(context);
    BusinessHelper.configureBeanProvider(provider);  // 静态注入
    return provider;
}
```

**设计原因：** 核心层（`BusinessHelper`、`BusinessAssembly`）不依赖 Spring，通过静态方法接收 SPI 实现，使得框架核心可以在非 Spring 环境中使用，减少对 Spring 的耦合。自动配置层负责桥接 Spring 和核心层。

### 9.4 反射与缓存优化

`CacheInvoke` 提供全局 Method 缓存，减少反射开销：

```java
public class CacheInvoke {
    private static final Map<String, Method> cacheMethod;  // 全局缓存
    private final boolean isStrict;                         // 严格模式

    // 缓存 key: "className.methodName(paramType1,paramType2)"
    public static Object cacheInvoke(boolean isStrict, Object service,
                                     String methodName,
                                     List<Class<?>> parameterTypes,
                                     Object... args);
}
```

- **严格模式**：方法未找到时抛异常
- **宽松模式**：方法未找到时静默返回 null

### 9.5 异步任务模型

```java
// 懒加载任务：首次 get() 才执行
LazyFutureTask<V> extends FutureTask<V>
    ├── started: AtomicBoolean    // 保证只执行一次
    ├── start(): 手动触发
    └── get(): 自动触发 start()

// 业务任务：自动注册到 DealVO
BusinessFutureTask<V> extends LazyFutureTask<V>
    └── 构造时自动 addFutureTask 到 DealVO.ALL_TASKS
```

**执行时机：** 所有异步任务在 `Assembly.complete()` 的 `finish()` 阶段，通过 `DealVO.executeAllFutureTask()` 统一触发。

## 十、快速开始

### 10.1 引入依赖

**Gradle：**
```groovy
implementation 'com.tianrenservice:orch-framework-spring-boot-starter:1.0.0'
```

**Maven：**
```xml
<dependency>
    <groupId>com.tianrenservice</groupId>
    <artifactId>orch-framework-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 仅使用核心功能（非 Spring 环境）时引入 `orch-framework-core` 并手动配置 SPI。

### 10.2 最小接入示例 (4-5个类)

**1. 定义输入 VO：**

```java
@Getter @Setter
public class OrderVO extends UserBusinessVO {
    private String orderId;
    private BigDecimal amount;

    @Override
    public BusinessTypeIdentifier getBusinessType() {
        return new SimpleBusinessType("order", "订单处理", OrderVO.class);
    }
}
```

**2. 定义输出 DealVO：**

```java
@Getter @Setter
public class OrderDealVO extends UserBusinessDealVO<OrderEntity> {
    private String orderStatus;
    private String resultMessage;

    public OrderDealVO(UserBusiness userBusiness) {
        super(userBusiness);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends BusinessDealVO<OrderEntity>> V doBuild(OrderEntity entity) {
        this.orderStatus = "COMPLETED";
        this.resultMessage = "订单处理成功";
        return (V) this;
    }
}
```

**3. 定义 Env（可选，有外部交互时使用）：**

```java
@Component
@Scope(SCOPE_PROTOTYPE)
@RecordAndReplay
public class OrderEnv extends BusinessEnv {

    @Autowired
    private InventoryService inventoryService;

    public boolean checkStock(String productId) {
        return inventoryService.hasStock(productId);
    }
}
```

**4. 定义 Entity：**

```java
public class OrderEntity extends BusinessEntity<BusinessHelper<OrderVO>> {

    public OrderEntity(BusinessHelper<OrderVO> businessHelper) {
        super(businessHelper);
    }

    @AfterProcess(Phase.SAVE_DB)
    public void saveOrder() {
        // 保存订单到数据库
        OrderEnv env = getEnv(OrderEnv.class);
        // ... 使用 env 中的服务
    }

    @AfterProcess(Phase.DEL_REDIS)
    public void clearCache() {
        // 清除相关缓存
    }
}
```

**5. 定义 Facade：**

```java
@Component
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {

    @Override
    public String getAssemblyTypeCode() {
        return "order";
    }

    @Override
    public void doProcess(OrderEntity entity, OrderVO vo) {
        OrderEnv env = entity.getEnv(OrderEnv.class);
        boolean hasStock = env.checkStock(vo.getOrderId());
        // ... 业务逻辑
    }
}
```

**6. 注册业务类型：**

**方式一：使用 SimpleBusinessType（推荐）**

```java
@Configuration
public class OrderTypeConfig {
    @Bean
    public BusinessTypeIdentifier orderType() {
        return new SimpleBusinessType("order", "订单处理", OrderVO.class);
    }
}
```

框架的 `OrchFrameworkAutoConfiguration` 会通过 `ObjectProvider` 自动收集所有 `BusinessTypeIdentifier` Bean 并注册到 `TypeRegistry`。无需手动调用 `registry.register()`。

**方式二：使用枚举（复杂场景）**

```java
public enum MyBusinessType implements BusinessTypeIdentifier {
    ORDER("order", "订单处理", OrderVO.class),
    PAYMENT("payment", "支付处理", PaymentVO.class);

    private final String code;
    private final String description;
    private final Class<? extends UserBusinessVO> voClass;

    // constructor + getter methods...
}

// 枚举需要逐个注册为 Bean
@Bean public BusinessTypeIdentifier orderType() { return MyBusinessType.ORDER; }
@Bean public BusinessTypeIdentifier paymentType() { return MyBusinessType.PAYMENT; }
```

**调用方式：**

```java
@Autowired
private OrderFacade orderFacade;

public void handleOrder(OrderVO orderVO) {
    OrderDealVO result = orderFacade.process(orderVO);
    // result 包含完整的处理结果
}
```

### 10.3 自定义 Helper 扩展示例

**场景：添加用户标签查询能力**

```java
public class OrderHelper extends BusinessHelper<OrderVO> {

    public UserTag getUserTag(String userId) {
        UserTagService service = BeanProvider.getBean(UserTagService.class);
        return service.queryTag(userId);
    }

    @Override
    public void saveDB() {
        // 自定义保存逻辑
    }
}

// Facade 中覆写 getBusinessHelper()
@Component
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {

    @Override
    public BusinessHelper<OrderVO> getBusinessHelper() {
        return new OrderHelper();
    }

    // ...
}
```

### 10.4 自定义 Env 与录制回放示例

**场景：添加 A/B 实验能力**

```java
@Component
@Scope(SCOPE_PROTOTYPE)
@RecordAndReplay
public class OrderEnv extends BusinessEnv {

    @Autowired
    private AbTestService abTestService;

    @Autowired
    private PaymentGateway paymentGateway;

    // 此方法调用和返回值会被自动录制
    public String getExperimentGroup(String userId, String experimentId) {
        return abTestService.getGroup(userId, experimentId);
    }

    // 此方法同样会被录制
    public PaymentResult processPayment(String orderId, BigDecimal amount) {
        return paymentGateway.charge(orderId, amount);
    }
}
```

> 标注 `@RecordAndReplay` 后，`OrderEnv` 上的所有方法在 RECORD 模式下会自动记录调用参数和返回值，在 REPLAY/CHECK 模式下会从录制数据中查找匹配结果直接返回，无需真正调用外部服务。

### 10.5 测试用例录制与回放示例

**场景：录制订单流程测试用例**

```java
@Autowired
private OrderFacade orderFacade;

@Autowired
private TestCasePersistenceService persistenceService;

@Autowired
private JsonSerializer jsonSerializer;

// 构建测试引擎
TestCaseEngine engine = new TestCaseEngine(
    orderFacade, persistenceService, jsonSerializer);

// 1. 录制测试用例
TestCaseVO testCase = new TestCaseVO();
testCase.setName("正常订单处理");
testCase.setDescription("测试正常订单的完整处理流程");
testCase.setBusinessType("order");
TestCaseVO recorded = engine.record(testCase);

// 2. 校验测试用例 (回放并比较结果一致性)
boolean isConsistent = engine.check(recorded.getId());

// 3. 重播测试用例 (获取结果快照，不比较)
TestCaseVO replayed = engine.replay(recorded);

// 4. 重生成测试用例 (重新录制并更新)
TestCaseVO regenerated = engine.regenerate(recorded.getId(), recorded);
```

### 10.6 自定义 SPI 实现示例

**自定义 JsonSerializer（如使用 Gson）：**

```java
@Bean
public JsonSerializer jsonSerializer() {
    return new JsonSerializer() {
        private final Gson gson = new Gson();

        @Override
        public String toJson(Object object) {
            return gson.toJson(object);
        }

        @Override
        public <T> T fromJson(String json, Class<T> clazz) {
            return gson.fromJson(json, clazz);
        }

        @Override
        public Map<String, Object> toMap(String json) {
            return gson.fromJson(json,
                new TypeToken<Map<String, Object>>(){}.getType());
        }
    };
}
```

**自定义 TestCasePersistenceService（如使用数据库）：**

```java
@Service
public class DbTestCasePersistenceService implements TestCasePersistenceService {

    @Autowired
    private TestCaseRepository repository;

    @Override
    public void saveOrUpdate(TestCaseVO testCaseVO) {
        TestCaseEntity entity = convertToEntity(testCaseVO);
        repository.save(entity);
    }

    @Override
    public TestCaseVO load(Integer id) {
        return repository.findById(id)
                .map(this::convertToVO)
                .orElse(null);
    }
}
```

### 10.7 高级用法：自定义 Assembly

大多数场景下，使用 `DynamicAssembly` 即可（框架自动创建）。仅当需要多个业务单元的复杂编排时才需自定义 Assembly，同时需注册 `AssemblyTypeIdentifier`：

```java
public class OrderFlowAssembly extends BusinessAssembly {
    @Override
    public String getAssemblyTypeCode() { return "order-flow"; }

    // 自定义装配单元构建（CacheInvoke 反射调用）
    public BusinessAssemblyUnit<OrderDealVO, OrderEntity, OrderVO>
    doBuild(OrderDealVO v, OrderEntity t, OrderVO r) {
        return BusinessAssemblyUnit.doBuild(v, t, r, this);
    }
}

// 注册 Assembly 类型
@Bean
public AssemblyTypeIdentifier orderFlowAssembly() {
    return new AssemblyTypeIdentifier() {
        public String getCode() { return "order-flow"; }
        public String getDescription() { return "订单流程装配线"; }
        public Class<? extends BusinessAssembly> getAssemblyClass() {
            return OrderFlowAssembly.class;
        }
    };
}
```

## 十一、最佳实践

### 11.1 开发规范

#### 11.1.1 命名规范

| 类型 | 命名规范 | 示例 |
|------|---------|------|
| 输入 VO | `XxxVO extends UserBusinessVO` | `OrderVO`, `PaymentVO` |
| 输出 DealVO | `XxxDealVO extends UserBusinessDealVO` | `OrderDealVO` |
| Entity | `XxxEntity extends BusinessEntity` | `OrderEntity` |
| Facade | `XxxFacade extends BusinessFacade` | `OrderFacade` |
| Env | `XxxEnv extends BusinessEnv` | `OrderEnv` |
| Helper | `XxxHelper extends BusinessHelper`（可选） | `OrderHelper` |
| 业务类型编码 | 小写英文，短横线分隔 | `"order"`, `"payment-refund"` |

#### 11.1.2 注解使用规范

```java
// Env 必须声明为 Prototype 作用域
@Component
@Scope(SCOPE_PROTOTYPE)    // 必须！每次请求创建新实例
@RecordAndReplay           // 启用录制回放
public class XxxEnv extends BusinessEnv { }

// Entity 生命周期优先使用注解
@AfterProcess(Phase.SAVE_DB)     // 优于覆写 Helper.saveDB()
@AfterProcess(Phase.DEL_REDIS)   // 优于覆写 Helper.delRedis()
@AfterProcess(Phase.FINISH)      // 优于覆写 Helper.finish()
```

> **注意：** 当 Entity 上存在 `@AfterProcess` 注解时，框架优先执行注解方法，不会调用 Helper 的对应方法。两种方式不要在同一 Phase 上混用。

#### 11.1.3 Env 方法规范

```java
// ✅ 正确：类级标注 @RecordAndReplay，所有方法自动录制
@Component @Scope(SCOPE_PROTOTYPE) @RecordAndReplay
public class OrderEnv extends BusinessEnv {
    public UserInfo queryUserInfo(String userId) {
        return userService.getById(userId);
    }
}

// ✅ 正确：方法级标注，指定方法标识
public class OrderEnv extends BusinessEnv {
    @RecordAndReplay("queryUserInfo")
    public UserInfo queryUserInfo(String userId) {
        return userService.getById(userId);
    }
}

// ✅ 正确：使用 value="ignore" 跳过特定方法的录制
@RecordAndReplay
public class OrderEnv extends BusinessEnv {
    @RecordAndReplay("ignore")
    public void logInfo(String msg) {
        // 此方法不参与录制回放
    }
}

// ❌ 错误：未标注注解，方法不会被录制/回放
public class OrderEnv extends BusinessEnv {
    public UserInfo queryUserInfo(String userId) {
        return userService.getById(userId);
    }
}
```

#### 11.1.4 异常处理规范

```java
// 需要中断整个流程
throw new InterruptException("订单 %s 不存在", orderId);

// 需要跳过当前步骤但继续流程
throw new SkipException("库存不足，跳过预留");

// 需要降级处理
throw new DegradeException("支付网关超时",
    () -> fallbackPayment(orderId));
```

### 11.2 性能优化

#### 11.2.1 反射缓存

框架内置 `CacheInvoke` 对 Method 对象进行全局缓存，消费方无需额外处理反射性能问题。

#### 11.2.2 异步任务模式

```java
// 在 Entity 中注册异步任务
new BusinessFutureTask<>(dealVO, () -> {
    // 异步逻辑：发送通知、更新统计等
    notificationService.send(orderId);
    return null;
});
// 任务在 complete() 阶段统一触发
```

#### 11.2.3 Env Prototype 作用域

`BusinessEnv` 必须为 Prototype 作用域，确保每次业务请求独立实例，避免并发下的数据污染。

### 11.3 测试最佳实践

#### 11.3.1 单元测试

核心模块纯 Java，无需 Spring 容器即可单元测试：

```java
// 直接构造 Handler 进行测试
JsonSerializer serializer = new JacksonJsonSerializer();
RecordAndReplayHandler handler = new RecordAndReplayHandler(serializer);

// 手动配置 SPI
BusinessHelper.configureBeanProvider(testBeanProvider);
BusinessAssembly.configure(testTypeRegistry, serializer);
```

#### 11.3.2 集成测试 (使用测试用例系统)

```java
// 1. 先录制一次真实流程
TestCaseVO recorded = engine.record(testCase);

// 2. 后续使用 check() 进行回归验证
boolean ok = engine.check(recorded.getId());

// 3. 接口变更后使用 regenerate() 更新基线
engine.regenerate(recorded.getId(), recorded);
```

#### 11.3.3 测试模式选择指南

| 场景 | 推荐模式 | 说明 |
|------|---------|------|
| 首次录制基线 | `RECORD` | 录制真实交互并保存 |
| CI/CD 回归验证 | `CHECK` | 严格比较，不一致则失败 |
| 人工排查问题 | `REVIEW` | 回放执行，观察差异 |
| 功能迭代后更新基线 | `REGENERATE` | 重新录制并更新存储 |
| 调试验证 | `REPLAY` | 宽松匹配，未匹配则执行 |

### 11.4 故障排查

#### 11.4.1 日志追踪

```java
// 使用 identity 追踪完整链路
log.info("业务处理开始，identity={}, userId={}",
    business.getIdentity(), userId);

// Assembly 也有独立 identity
log.info("装配线创建，assemblyIdentity={}, mode={}",
    assembly.getIdentity(), assembly.getMode());
```

#### 11.4.2 常见问题排查

| 现象 | 可能原因 | 排查方向 |
|------|---------|---------|
| `NullPointerException` on Env | Env 未声明 `@Scope(PROTOTYPE)` | 检查注解配置 |
| 录制数据为空 | 方法未标注 `@RecordAndReplay` | 检查 Env 类/方法注解 |
| 回放匹配失败 | 参数发生变化 | 检查 `argsMatch` 日志 |
| TypeRegistry 找不到类型 | 未声明为 `@Bean` | 确认 `BusinessTypeIdentifier` 已注册 |
| Assembly 创建失败 | `getAssemblyTypeCode()` 返回 null | 检查 Facade 实现 |

#### 11.4.3 常见错误信息

```java
// 错误：InterruptException("测试用例数据异常，缺少 businessType 字段")
// 原因：测试用例 JSON 中缺少 businessType
// 解决：检查 TestCaseVO 的 multiBusinessVO 字段

// 错误：InterruptException("未找到方法xxx的记录")
// 原因：CHECK 模式下，covers 中没有对应方法的记录
// 解决：重新录制测试用例，或切换到 REPLAY 模式

// 错误：InterruptException("组合线数据注入中断")
// 原因：assembly.build() 失败，通常是类型注册问题
// 解决：检查 TypeRegistry 中是否注册了对应的 BusinessType

// 错误：InterruptException("构造处理结果失败")
// 原因：DealVO 子类缺少 UserBusiness 参数的构造器
// 解决：确保 DealVO 有 public XxxDealVO(UserBusiness userBusiness) 构造器
```

#### 11.4.4 测试用例重现

当生产环境出现问题时，可通过以下步骤重现：

1. 使用 `RECORD` 模式在测试环境录制相同输入的流程
2. 查看 `TestCaseVO.multiBusinessEnv` 中的交互记录
3. 使用 `REVIEW` 模式逐步回放，定位异常节点

## 十二、常见问题 FAQ

### 12.1 架构相关

**Q: 为什么核心模块不依赖 Spring？**

A: 实现框架无关性。核心模块纯 Java 编写，可在任何 Java 环境中使用（Spring、Quarkus、Micronaut 等），也便于纯单元测试，不需要启动 Spring 容器。

**Q: 消费方必须使用 Spring Boot 吗？**

A: 不必须。使用 `orch-framework-core` 并手动配置 SPI（`BusinessHelper.configureBeanProvider()`、`BusinessAssembly.configure()`），可在非 Spring 环境中运行。

**Q: DynamicAssembly 和自定义 Assembly 如何选择？**

A: 简单业务流程（单一 Facade，无需自定义 build 逻辑）使用 DynamicAssembly 即可，框架自动创建。多业务编排或需要自定义 build 逻辑时才需继承 `BusinessAssembly`。

**Q: 为什么 Helper 的 beanProvider 是静态字段？**

A: `beanProvider` 是框架级配置，整个应用生命周期只需设置一次。静态字段避免了在每个 Helper 实例中重复注入，简化了消费方代码。

**Q: BusinessAssembly 和 BusinessFacade 的区别是什么？**

A: `BusinessFacade` 是单个业务实体的处理门面，定义了 build → process → buildVO → afterProcess 的标准流程。`BusinessAssembly` 是多个业务实体的编排器，按顺序执行多个 Facade，并管理测试用例。

**Q: 为什么核心层使用静态方法配置 SPI，而不是 Spring 注入？**

A: 核心层（`BusinessHelper`、`BusinessAssembly`）设计为不依赖 Spring 容器。通过静态方法接收 SPI 实现，使得框架核心可以在非 Spring 环境中使用，减少对 Spring 的耦合。自动配置层负责桥接 Spring 和核心层。

**Q: 为什么 BusinessEnv 使用原型作用域？**

A: 每次业务处理需要独立的环境实例，避免多线程共享状态。每个实例有独立的 `nowTime`、`records`、`covers`，保证录制/回放的隔离性。`BusinessEnv` 基类是纯 POJO，Starter 将其注册为原型 Bean，消费方子类应添加 `@Component` + `@Scope(SCOPE_PROTOTYPE)`。

### 12.2 测试相关

**Q: 录制回放如何处理时间敏感的逻辑？**

A: `BusinessEnv` 的 `nowTime` 字段在录制时固定，回放时使用相同的时间戳，确保时间相关逻辑的一致性。

**Q: 回放模式下参数匹配的精度如何控制？**

A: `argsMatch()` 支持基本类型精确匹配和复杂对象的 JSON 序列化比较。消费方可覆写 `BusinessEnv.argsMatch()` 自定义匹配策略。

**Q: CHECK 和 REVIEW 模式有什么区别？**

A: 行为相同（严格匹配），语义不同。CHECK 用于自动化回归校验（CI/CD），REVIEW 用于人工复盘排查。

**Q: 测试用例录制的原理是什么？**

A: (1) `@RecordAndReplay` 注解标记的方法被 `RecordAndReplayAspect` 拦截 (2) Aspect 将调用委托给 `RecordAndReplayHandler`（纯 Java） (3) 参数标准化（复杂对象转 Map） (4) 记录方法名、参数、返回值到 `InteractionRecord` (5) 序列化为 JSON 通过 `TestCasePersistenceService` 持久化。

**Q: 测试用例录制会影响性能吗？**

A: LIVE 模式无影响，切面直接放行；RECORD 模式有轻微影响（序列化和存储），仅在测试环境使用；REPLAY 模式性能更好（不调用外部服务，直接返回记录结果）。

**Q: 测试用例的版本兼容性如何处理？**

A: 当类结构发生变化（字段增删）时，已有用例可能无法直接使用。建议使用 REGENERATE 模式重新生成，或删除旧用例重新录制。`JacksonJsonSerializer` 配置了忽略未知属性（`FAIL_ON_UNKNOWN_PROPERTIES = false`），可容忍部分字段变化。

### 12.3 性能相关

**Q: 反射调用的性能开销如何？**

A: `CacheInvoke` 对 `java.lang.reflect.Method` 对象进行全局缓存，首次查找后后续调用直接从缓存获取，性能接近直接调用。

**Q: Prototype 作用域的 Env 会不会频繁 GC？**

A: Env 实例随请求创建和回收，属于短生命周期对象。JVM 的年轻代 GC 对此类对象处理高效，通常不会成为瓶颈。

### 12.4 扩展相关

**Q: 如何替换 Jackson 为其他 JSON 库？**

A: 实现 `JsonSerializer` 接口并声明为 `@Bean`，Spring 的 `@ConditionalOnMissingBean` 会自动使用消费方的实现替代默认的 `JacksonJsonSerializer`。

**Q: 如何实现测试用例持久化？**

A: 实现 `TestCasePersistenceService` 接口并声明为 `@Bean`。框架不限定持久化方式，消费方可选择数据库、文件系统、远程服务等任何方案。

**Q: 如何在非 Spring 环境中使用录制回放？**

A: 直接调用 `RecordAndReplayHandler.handle()`，将实际方法调用封装为 `MethodInvoker` 回调传入：

```java
RecordAndReplayHandler handler = new RecordAndReplayHandler(jsonSerializer);
Object result = handler.handle(
    "methodName",
    ReturnType.class,
    methodAnnotation,
    classAnnotation,
    businessEnv,       // target 必须是 BusinessEnv 子类
    args,
    () -> actualMethod.invoke(target, args)  // MethodInvoker
);
```

## 十三、总结

### 13.1 核心优势

| 特性 | 说明 |
|------|------|
| **框架解耦** | 核心纯 Java，Spring 可选，单元测试无需容器 |
| **SPI 驱动扩展** | 7 个接口定义扩展点，`@Bean` 声明即可覆写 |
| **类型安全** | 泛型约束贯穿 Facade → Entity → Helper → VO 链路 |
| **自动类型收集** | `ObjectProvider` 自动发现并注册业务类型 |
| **声明式生命周期** | `@AfterProcess` 注解替代继承覆写 |
| **内置录制回放** | 六种业务模式，支持交互录制、回放、校验、复盘、重生成 |
| **测试用例管理** | `TestCaseEngine` 提供完整的录制-校验-重生成工作流 |
| **最小接入成本** | 4-5 个类即可接入完整生命周期 |
| **动态组合线** | `DynamicAssembly` 自动创建，减少样板代码 |
| **异步任务集成** | `LazyFutureTask` 懒加载，Pipeline 完成后统一触发 |

### 13.2 学习路径

```
入门：
  1. 理解 BusinessFacade.process() 的完整生命周期
  2. 掌握 VO → Entity → DealVO 的数据流转
  3. 实现最小接入示例 (4-5 个类)

进阶：
  4. 理解 @AfterProcess 注解驱动的生命周期
  5. 掌握 BusinessEnv + @RecordAndReplay 的录制回放机制
  6. 学习 BusinessAssembly 的管道编排模型

深入：
  7. 理解 SPI 扩展点体系
  8. 掌握 TestCaseEngine 的测试用例工作流
  9. 了解 CacheInvoke 反射缓存和 LazyFutureTask 异步模型
```

### 13.3 注意事项

1. **Env 必须 Prototype**：`BusinessEnv` 子类必须声明 `@Scope(SCOPE_PROTOTYPE)`，否则并发下数据污染
2. **Entity 无状态服务访问**：Entity 通过 `getEnv()` 访问外部服务，不要直接 `@Autowired`
3. **泛型声明完整性**：Facade 的三个泛型参数（V、T、R）必须完整声明，框架依赖反射解析
4. **测试用例 identity 忽略**：测试比较时自动移除 identity 字段（UUID 每次不同）
5. **@RecordAndReplay 目标限制**：只能标注在 `BusinessEnv` 或其子类上，Handler 会校验 target 类型
6. **静态配置初始化顺序**：`BeanProvider` 和 `TypeRegistry` 必须在业务代码执行前完成配置
7. **DealVO 构造器要求**：消费方的 DealVO 子类构造器必须接受 `UserBusiness` 参数（`buildVO()` 通过 `getConstructor(UserBusiness.class)` 反射构造）
8. **finish() 调用时机**：`entity.finish()` 在 `assembly.complete()` 内部调用，不在 `afterProcess()` 中
9. **doProcess VO 直接传入**：`doProcess(T, R)` 中 VO 直接传入，无需通过 `helper.getContext().getVo()` 获取
10. **@AfterProcess 不混用**：当 Entity 上存在 `@AfterProcess` 注解时，框架优先执行注解方法，不会调用 Helper 的对应方法。两种方式不要在同一 Phase 上混用
11. **使用框架异常体系**：使用 `InterruptException`/`SkipException`/`DegradeException`，不要直接抛 `RuntimeException`

------

**技术栈**: Java 21 + Spring Boot 4.0.2 + Gradle 8.14 (多模块) + Spring AOP + Jackson + Lombok
**项目结构**: `orch-framework-core`（纯 Java，零 Spring 依赖）+ `orch-framework-spring-boot-starter`（Spring Boot 自动配置）
**包名**: `com.tianrenservice.orch_framework`
**配置前缀**: `orch-framework`
