# AI-Framework-Spring 架构设计与开发指南

<!-- TOC -->
* [AI-Framework-Spring 架构设计与开发指南](#ai-framework-spring-架构设计与开发指南)
  * [一、整体架构设计](#一整体架构设计)
    * [1.1 架构分层](#11-架构分层)
    * [1.2 整体架构图](#12-整体架构图)
    * [1.3 核心类图](#13-核心类图)
    * [1.4 时序图 - 业务处理流程](#14-时序图---业务处理流程)
    * [1.5 数据流图](#15-数据流图)
    * [1.6 模块依赖关系](#16-模块依赖关系)
  * [二、核心抽象层设计 (core包) - 基础骨架](#二核心抽象层设计-core包---基础骨架)
    * [2.1 Business 基类体系](#21-business-基类体系)
    * [2.2 VO (Value Object) 体系](#22-vo-value-object-体系)
    * [2.3 BusinessFacade - 门面模式的深度应用](#23-businessfacade---门面模式的深度应用)
    * [2.4 BusinessHelper - 协作者模式](#24-businesshelper---协作者模式)
    * [2.5 BusinessEnv - 环境抽象与回放机制](#25-businessenv---环境抽象与回放机制)
    * [2.6 BusinessAssembly - 装配线模式](#26-businessassembly---装配线模式)
  * [三、Pipeline 编排引擎 (pipeline包)](#三pipeline-编排引擎-pipeline包)
    * [3.1 BusinessContext - 上下文传递](#31-businesscontext---上下文传递)
    * [3.2 BusinessAssemblyUnit - 装配单元](#32-businessassemblyunit---装配单元)
    * [3.3 BusinessEmptyAssembly - 空装配线](#33-businessemptyassembly---空装配线)
    * [3.4 DynamicAssembly - 动态装配线](#34-dynamicassembly---动态装配线)
  * [四、录制/回放系统 (record包) - 测试基础设施](#四录制回放系统-record包---测试基础设施)
    * [4.1 @RecordAndReplay 注解](#41-recordandreplay-注解)
    * [4.2 RecordAndReplayAspect - AOP切面](#42-recordandreplayaspect---aop切面)
    * [4.3 InteractionRecord - 交互记录](#43-interactionrecord---交互记录)
    * [4.4 六种运行模式](#44-六种运行模式)
  * [五、测试用例管理 (testcase包)](#五测试用例管理-testcase包)
    * [5.1 TestCaseVO - 测试用例数据](#51-testcasevo---测试用例数据)
    * [5.2 TestCaseRunner - 测试执行器](#52-testcaserunner---测试执行器)
    * [5.3 TestCaseEngine - 测试引擎](#53-testcaseengine---测试引擎)
    * [5.4 TestCaseComparator - 结果比对](#54-testcasecomparator---结果比对)
  * [六、SPI 扩展点设计 (spi包)](#六spi-扩展点设计-spi包)
    * [6.1 TypeRegistry - 类型注册中心](#61-typeregistry---类型注册中心)
    * [6.2 JsonSerializer - JSON序列化](#62-jsonserializer---json序列化)
    * [6.3 BeanProvider - Bean查找](#63-beanprovider---bean查找)
    * [6.4 TestCasePersistenceService - 测试用例持久化](#64-testcasepersistenceservice---测试用例持久化)
    * [6.5 类型标识接口](#65-类型标识接口)
  * [七、自动配置 (autoconfigure包)](#七自动配置-autoconfigure包)
    * [7.1 AiFrameworkAutoConfiguration](#71-aiframeworkautoconfiguration)
    * [7.2 默认实现](#72-默认实现)
    * [7.3 配置属性](#73-配置属性)
  * [八、异常体系 (exception包)](#八异常体系-exception包)
  * [九、工具类 (util包)](#九工具类-util包)
  * [十、设计模式与编程范式](#十设计模式与编程范式)
    * [10.1 使用的设计模式](#101-使用的设计模式)
    * [10.2 泛型编程](#102-泛型编程)
    * [10.3 依赖注入模式](#103-依赖注入模式)
  * [十一、代码示例](#十一代码示例)
    * [11.1 注册业务类型](#111-注册业务类型)
    * [11.2 实现业务实体与门面](#112-实现业务实体与门面)
    * [11.3 录制与回放测试用例](#113-录制与回放测试用例)
    * [11.4 自定义SPI实现](#114-自定义spi实现)
    * [11.5 高级用法：自定义Helper与Assembly](#115-高级用法自定义helper与assembly)
  * [十二、最佳实践](#十二最佳实践)
    * [12.1 开发规范](#121-开发规范)
    * [12.2 测试最佳实践](#122-测试最佳实践)
    * [12.3 故障排查](#123-故障排查)
  * [十三、常见问题FAQ](#十三常见问题faq)
    * [13.1 架构相关](#131-架构相关)
    * [13.2 测试相关](#132-测试相关)
    * [13.3 扩展相关](#133-扩展相关)
  * [十四、总结](#十四总结)
    * [14.1 核心优势](#141-核心优势)
    * [14.2 学习路径](#142-学习路径)
    * [14.3 注意事项](#143-注意事项)
<!-- TOC -->

## 一、整体架构设计

### 1.1 架构分层

这是一个**可复用的业务流程编排框架**，内置录制/回放能力，构建为 Spring Boot 4.0.2 自动配置库。整体采用**三层架构**：

```
┌─────────────────────────────────────────────┐
│  Consumer Layer (业务消费层)                │  ← 接入方实现
├─────────────────────────────────────────────┤
│  Core Framework Layer (核心框架层)          │  ← 本框架提供
│  Pipeline + Record + TestCase + SPI         │
├─────────────────────────────────────────────┤
│  Auto-Configuration Layer (自动配置层)      │  ← Spring Boot Starter
└─────────────────────────────────────────────┘
```

### 1.2 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Consumer Application (接入方)                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  MyService                                                    │  │
│  │  - handle(MyVO) → myFacade.process(myVO)                      │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Custom Domain (消费方最小代码: 4~5个类)                      │  │
│  │  MyVO, MyDealVO, MyEnv, MyEntity, MyFacade                    │  │
│  │  (Helper可选 · Assembly自动推导 · 类型注册声明@Bean即可)      │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────────┐
│                  Core Framework (核心框架层)                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Pipeline Engine (编排引擎)                                   │  │
│  │  BusinessFacade<V,T,R>, BusinessAssembly, DynamicAssembly     │  │
│  │  BusinessAssemblyUnit<V,T,R>, BusinessContext<T>               │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Entity Model (实体模型)                                      │  │
│  │  Business, UserBusiness, BusinessEntity<O>, BusinessHelper<R> │  │
│  │  @AfterProcess (注解驱动生命周期)                              │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Record & Replay (录制/回放)                                  │  │
│  │  @RecordAndReplay, RecordAndReplayAspect, BusinessEnv         │  │
│  │  InteractionRecord                                             │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Test Case Management (测试用例管理)                          │  │
│  │  TestCaseEngine, TestCaseRunner, TestCaseComparator            │  │
│  │  TestCaseVO                                                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  SPI Extension Points (扩展点)                                │  │
│  │  TypeRegistry, JsonSerializer, BeanProvider                    │  │
│  │  SimpleBusinessType, TestCasePersistenceService                │  │
│  │  BusinessTypeIdentifier, AssemblyTypeIdentifier                │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────────┐
│              Auto-Configuration (自动配置层)                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  AiFrameworkAutoConfiguration                                  │  │
│  │  - JacksonJsonSerializer (默认JSON实现)                       │  │
│  │  - SpringBeanProvider (默认Bean查找)                          │  │
│  │  - DefaultTypeRegistry (默认类型注册)                         │  │
│  │  - 自动收集 BusinessTypeIdentifier/AssemblyTypeIdentifier Bean│  │
│  │  - RecordAndReplayAspect (条件启用)                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.3 核心类图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Core Type System                         │
└─────────────────────────────────────────────────────────────────┘

Business (UUID标识: "business-" + UUID)
    │
    └── UserBusiness (用户ID)
            │
            ├── BusinessVO (输入数据 - 抽象)
            │       │
            │       └── UserBusinessVO
            │               └── [消费方自定义VO]
            │
            ├── BusinessEntity<O extends BusinessHelper> (业务实体)
            │       │
            │       ├── getEnv(Class) — Env快捷访问
            │       ├── @AfterProcess — 注解驱动生命周期钩子
            │       └── [消费方自定义Entity]
            │
            └── BusinessDealVO<T extends BusinessEntity> (输出数据)
                    │
                    └── UserBusinessDealVO<T>
                            └── [消费方自定义DealVO]

┌─────────────────────────────────────────────────────────────────┐
│                      Processing Pipeline                         │
└─────────────────────────────────────────────────────────────────┘

BusinessFacade<V, T, R>   (3个泛型: DealVO, Entity, InputVO)
    │
    ├── process(R) : V
    │       │
    │       ├── 1. assembly = createForType(getAssemblyTypeCode())
    │       │       └── 自动创建 DynamicAssembly 或已注册的Assembly
    │       │
    │       ├── 2. assembly.build(tClazz, vClazz, r)
    │       │       └── 注册装配单元
    │       │
    │       ├── 3. helper = getBusinessHelper()
    │       │       └── 默认返回 new BusinessHelper<>()，可覆写
    │       │
    │       ├── 4. BusinessHelper.build(helper, tClazz)
    │       │       └── 反射构建BusinessEntity
    │       │
    │       ├── 5. assembly.ready(entity, vo)
    │       │       └── 装配单元就绪
    │       │
    │       ├── 6. doProcess(entity, vo) [Abstract]
    │       │       └── 子类实现业务逻辑（VO直接传入）
    │       │
    │       ├── 7. entity.buildVO(vClazz)
    │       │       └── 构建输出VO
    │       │
    │       ├── 8. entity.afterProcess()
    │       │       ├── 扫描 @AfterProcess(SAVE_DB) 注解方法
    │       │       ├── 扫描 @AfterProcess(DEL_REDIS) 注解方法
    │       │       └── 回退到 helper.saveDB() / helper.delRedis()
    │       │
    │       └── 9. assembly.complete(vo, entity, businessVO)
    │               └── 装配单元完成 + 测试用例处理
    │
    ├── getAssemblyTypeCode() [Abstract] — 返回装配线类型编码
    ├── getBusinessHelper() — 默认实现，消费方可覆写
    │
    └── Implementations:
            └── [消费方自定义Facade]

┌─────────────────────────────────────────────────────────────────┐
│                      Assembly Pipeline                           │
└─────────────────────────────────────────────────────────────────┘

BusinessAssembly (装配线基类)
    │
    ├── businessAssemblyUnits: List<BusinessAssemblyUnit>
    │       ├── Unit[0]: {context, entity, dealVO, order=1}
    │       ├── Unit[1]: {context, entity, dealVO, order=2}
    │       └── Unit[N]: {context, entity, dealVO, order=N}
    │
    ├── mode: BusinessMode (LIVE/RECORD/REPLAY/CHECK/REVIEW/REGENERATE)
    │
    ├── Test Case Data:
    │       ├── multiBusinessVO: Map<markName, BusinessVO>
    │       ├── multiBusinessEnv: Map<markName, Map<key, List<InteractionRecord>>>
    │       └── multiBusinessDealVO: Map<markName, BusinessDealVO>
    │
    ├── createForType(typeCode) — 工厂方法，自动推导Assembly类型
    │
    └── Implementations:
            ├── BusinessEmptyAssembly (空装配线，兜底)
            ├── DynamicAssembly (动态装配线，自动推导)
            └── [消费方自定义Assembly — 仅高级场景需要]
```

### 1.4 时序图 - 业务处理流程

```
Client          Facade          Assembly        Entity          Helper          Env
  │               │               │               │               │               │
  │──process(R)──>│               │               │               │               │
  │               │               │               │               │               │
  │               │──createForType()──>           │               │               │
  │               │  (DynamicAssembly)             │               │               │
  │               │               │               │               │               │
  │               │──assembly.build()─>│           │               │               │
  │               │               │  注册装配单元  │               │               │
  │               │               │               │               │               │
  │               │──getBusinessHelper()           │               │               │
  │               │  (默认 new BusinessHelper)     │               │               │
  │               │               │               │               │               │
  │               │──BusinessHelper.build()────────>│              │               │
  │               │               │  构建Entity    │               │               │
  │               │               │               │──setContext()─>│               │
  │               │               │               │               │──getEnv()────>│
  │               │               │               │               │  (Prototype)  │
  │               │               │               │               │<──BusinessEnv─│
  │               │               │<──Entity───────│               │               │
  │               │               │               │               │               │
  │               │──assembly.ready()>│            │               │               │
  │               │               │  装配单元就绪  │               │               │
  │               │               │               │               │               │
  │               │──doProcess(entity, vo)────────>│               │               │
  │               │               │  [业务逻辑]    │               │               │
  │               │               │               │──getEnv(Cls)─>│               │
  │               │               │               │  (快捷方法)    │               │
  │               │               │               │               │──envMethod()─>│
  │               │               │               │               │  @RecordAndReplay
  │               │               │               │               │<──result──────│
  │               │               │               │<──result──────│               │
  │               │               │<──完成─────────│               │               │
  │               │               │               │               │               │
  │               │──entity.buildVO()────────────>│               │               │
  │               │<──DealVO──────│               │               │               │
  │               │               │               │               │               │
  │               │──entity.afterProcess()────────>│              │               │
  │               │               │               │──@AfterProcess(SAVE_DB)       │
  │               │               │               │──@AfterProcess(DEL_REDIS)     │
  │               │               │               │  (无注解时回退到Helper)        │
  │               │               │               │──entity.finish()              │
  │               │               │               │──@AfterProcess(FINISH)        │
  │               │               │               │               │               │
  │               │──assembly.complete()>│         │               │               │
  │               │               │  装配单元完成  │               │               │
  │               │               │  (填充测试数据)│               │               │
  │               │               │               │               │               │
  │               │               │  [RECORD模式] │               │               │
  │               │               │──saveTestCase()               │               │
  │               │               │  持久化测试用例│               │               │
  │               │               │               │               │               │
  │<──result──────│               │               │               │               │
  │               │               │               │               │               │
```

### 1.5 数据流图

```
┌─────────────────────────────────────────────────────────────────┐
│                          Data Flow                               │
└─────────────────────────────────────────────────────────────────┘

消费方请求
    │
    └─ 业务输入数据 (UserBusinessVO子类)
    │
    ▼
┌─────────────────┐
│  BusinessFacade  │ 门面入口
│  process(R)      │
│  ├─ createForType(getAssemblyTypeCode())  → Assembly自动推导
│  └─ getBusinessHelper()                    → 默认实现或自定义
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────────┐
│ BusinessAssembly│─────>│ AssemblyUnit     │ 装配单元
│ / DynamicAssembly      │                  │
│ • build()       │      │ • context        │
│ • ready()       │      │ • entity         │
│ • complete()    │      │ • dealVO         │
└─────────────────┘      │ • markName       │
         │               └──────────────────┘
         ▼
┌─────────────────┐      ┌──────────────────┐
│ BusinessEntity  │─────>│ BusinessHelper   │ 协作者(可选自定义)
│                 │      │                  │
│ • doProcess(R)  │      │ • context        │
│ • getEnv(Class) │      │ • env            │
│ • buildVO()     │      │ • saveDB()       │
│ • @AfterProcess │      │ • delRedis()     │
└─────────────────┘      │ • finish()       │
         │               └──────────────────┘
         │                        │
         │                        ▼
         │               ┌──────────────────┐
         │               │ BusinessEnv      │ 环境
         │               │                  │
         │               │ • getNowTime()   │
         │               │ • records        │
         │               │ • covers         │
         │               │ @RecordAndReplay │
         │               └──────────────────┘
         ▼
┌─────────────────┐
│ BusinessDealVO  │ 输出结果
│                 │
│ • identity      │
│ • scope         │
│ • nowTime       │
│ • futureTasks   │
└─────────────────┘
         │
         ▼
    测试用例持久化 (RECORD模式)
    结果比对验证 (CHECK/REVIEW模式)
```

### 1.6 模块依赖关系

```
┌─────────────────────────────────────────────────────────────────┐
│                      Module Dependencies                         │
└─────────────────────────────────────────────────────────────────┘

core.constant
    └── BusinessMode (六种运行模式)

core.spi (扩展点接口)
    ├── TypeRegistry
    ├── JsonSerializer
    ├── BeanProvider
    ├── BusinessTypeIdentifier
    ├── AssemblyTypeIdentifier
    ├── ScopeIdentifier
    └── TestCasePersistenceService

    ↑ (依赖)

core.exception (异常体系)
    ├── BusinessException
    ├── InterruptException
    ├── SkipException
    └── DegradeException

    ↑ (依赖)

core.entity (实体模型)
    ├── Business
    ├── UserBusiness
    ├── BusinessEntity<O>    ← 支持 @AfterProcess + getEnv()
    └── BusinessHelper<R>

    ↑ (依赖)

core.vo (值对象)
    ├── BusinessVO
    ├── UserBusinessVO
    ├── BusinessDealVO<T>
    └── UserBusinessDealVO<T>

    ↑ (依赖)

core.annotation (注解)
    └── @AfterProcess(Phase)  ← SAVE_DB, DEL_REDIS, FINISH

    ↑ (依赖)

core.record (录制/回放)
    ├── @RecordAndReplay
    ├── RecordAndReplayAspect
    ├── BusinessEnv
    └── InteractionRecord

    ↑ (依赖)

core.pipeline (编排引擎)
    ├── BusinessFacade<V, T, R>
    ├── BusinessAssembly
    ├── DynamicAssembly
    ├── BusinessAssemblyUnit<V, T, R>
    ├── BusinessContext<T>
    └── BusinessEmptyAssembly

    ↑ (依赖)

core.testcase (测试用例)
    ├── TestCaseVO
    ├── TestCaseRunner
    ├── TestCaseEngine
    └── TestCaseComparator

    ↑ (依赖)

core.util (工具类)
    ├── CacheInvoke
    ├── BeanUtil
    ├── ObjectCompareUtil
    ├── LazyFutureTask<V>
    └── BusinessFutureTask<V>

    ↑ (依赖)

autoconfigure (自动配置)
    ├── AiFrameworkAutoConfiguration
    ├── AiFrameworkProperties
    ├── JacksonJsonSerializer
    ├── SpringBeanProvider
    └── DefaultTypeRegistry
```

------

## 二、核心抽象层设计 (core包) - 基础骨架

### 2.1 Business 基类体系

**设计哲学：** 采用**严格的类型层次体系**，通过泛型约束确保类型安全：

```java
Business (UUID标识: "business-" + UUID)
    └── UserBusiness (用户业务标识: userId)
        └── BusinessEntity<O extends BusinessHelper> (业务实体)
            └── [消费方自定义Entity]
```

**核心特点：**

1. **Identity模式**：每个业务对象都有唯一的UUID标识（`"business-" + UUID`），便于链路追踪和调试
2. **组合优于继承**：通过`BusinessHelper`注入依赖，而非深层继承
3. **作用域隔离**：每个Entity可通过`ScopeIdentifier`标识其业务域

### 2.2 VO (Value Object) 体系

**双层VO设计：**

```java
BusinessVO (输入) ←→ BusinessEntity (处理) ←→ BusinessDealVO (输出)
```

**设计亮点：**

- **输入VO** (`UserBusinessVO`): 携带业务输入数据，通过`getBusinessType()`关联`BusinessTypeIdentifier`
- **输出VO** (`UserBusinessDealVO<T>`): 携带处理结果、作用域、时间戳和异步任务
- **泛型约束**: `BusinessDealVO<T extends BusinessEntity<?>>`确保VO与实体的强关联
- **空对象模式**: `buildEmpty(Class<T>)`通过反射创建空实例，避免null

```java
// 输入VO - 抽象基类
public abstract class UserBusinessVO extends BusinessVO {
    private String userId;
    public abstract BusinessTypeIdentifier getBusinessType();
}

// 输出VO - 抽象基类
public abstract class UserBusinessDealVO<T extends BusinessEntity<?>> extends BusinessDealVO<T> {
    private String userId;
    public abstract <V extends BusinessDealVO<T>> V doBuild(T entity);
}
```

### 2.3 BusinessFacade - 门面模式的深度应用

```java
public abstract class BusinessFacade<
    V extends UserBusinessDealVO<T>,    // 输出VO
    T extends BusinessEntity<?>,         // 实体
    R extends UserBusinessVO>            // 输入VO
```

**核心流程：**

```
process(R) → createForType() → assembly.build() → getBusinessHelper()
           → BusinessHelper.build() → assembly.ready()
           → doProcess(T, R) → buildVO() → afterProcess() → assembly.complete()
```

**职责分离：**

1. **createForType()**: 根据`getAssemblyTypeCode()`自动创建Assembly（DynamicAssembly或已注册类型）
2. **assembly.build()**: 注册装配单元到装配线
3. **getBusinessHelper()**: 获取Helper实例（默认`new BusinessHelper<>()`，消费方可覆写返回自定义Helper）
4. **BusinessHelper.build()**: 反射构建领域实体，设置上下文
5. **assembly.ready()**: 装配单元就绪
6. **doProcess(T, R)**: 执行业务逻辑（子类实现，VO直接传入）
7. **buildVO()**: 构建输出视图
8. **afterProcess()**: 后处理（扫描`@AfterProcess`注解，回退到Helper的saveDB/delRedis）
9. **assembly.complete()**: 装配单元完成，填充测试数据

**关键抽象方法：**

```java
/** 返回装配线类型编码，框架据此创建 Assembly 实例 */
public abstract String getAssemblyTypeCode();

/** 业务逻辑入口，VO直接传入 */
public abstract void doProcess(T t, R r);

/** 返回Helper实例，默认new BusinessHelper<>()，消费方可覆写 */
public BusinessHelper<R> getBusinessHelper() {
    return new BusinessHelper<>();
}
```

**异常处理策略：**

```java
try {
    // 正常流程
} catch (SkipException e) {
    // 跳过当前操作，返回null
} catch (DegradeException e) {
    // 降级处理，返回null
} catch (Exception e) {
    // 包装为InterruptException向上抛出
    throw new InterruptException("businessFacade process interrupt", e);
}
```

### 2.4 BusinessHelper - 协作者模式

**核心职责：**

```java
BusinessHelper<R extends UserBusinessVO>
    ├── businessContext: BusinessContext<R>   // 上下文引用
    ├── businessEnv: BusinessEnv              // 环境（延迟加载）
    ├── beanProvider: static BeanProvider     // Bean查找SPI
    │
    ├── build(O, Class<T>)                   // 反射构建Entity
    ├── getBusinessEnv(Class<E>)             // 延迟加载Env
    ├── getNowTime()                         // 从Env获取时间
    ├── saveDB()                             // 生命周期钩子（空实现）
    ├── delRedis()                           // 生命周期钩子（空实现）
    └── finish()                             // 生命周期钩子（空实现）
```

**设计原则：**

1. **职责单一**: Helper只关注上下文管理和生命周期钩子
2. **延迟初始化**: `BusinessEnv`按需加载并缓存
3. **SPI依赖**: 通过静态`BeanProvider`查找Spring Bean，解耦框架与Spring
4. **可选自定义**: 简单场景下使用框架默认`BusinessHelper<R>`即可，复杂场景才需要自定义子类
5. **注解替代**: `@AfterProcess`注解可直接在Entity上定义生命周期钩子，无需覆写Helper方法

### 2.5 BusinessEnv - 环境抽象与回放机制

**核心创新：时间旅行测试 (Time Travel Testing)**

```java
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BusinessEnv {
    private final long nowTime = System.currentTimeMillis();

    @RecordAndReplay
    public long getNowTime() {
        return getRealNowTime();
    }
}
```

**六种运行模式：**

1. **LIVE**: 正常模式 - 直接执行
2. **RECORD**: 记录模式 - 执行并记录所有外部交互
3. **REPLAY**: 回放模式 - 优先使用记录数据，找不到则直接执行
4. **CHECK**: 检查模式 - 严格验证，找不到记录则抛异常
5. **REVIEW**: 复盘模式 - 同CHECK，用于回放验证
6. **REGENERATE**: 重生成模式 - 同CHECK，但会更新测试用例

**实现机制：**

- **AOP切面拦截**: `RecordAndReplayAspect`拦截所有`@RecordAndReplay`标记的方法
- **参数标准化**: 将复杂对象转为Map进行比对
- **智能匹配**: `findMatchingRecord()`根据参数特征匹配历史记录
- **原型作用域**: `@Scope(SCOPE_PROTOTYPE)`保证每次调用都是新实例

### 2.6 BusinessAssembly - 装配线模式

**装配单元设计：**

```java
BusinessAssembly (装配线)
    └── List<BusinessAssemblyUnit> (装配单元列表)
        └── BusinessAssemblyUnit
            ├── businessContext  // 业务上下文
            ├── businessEntity   // 业务实体
            ├── businessDealVO   // 输出VO
            ├── order            // 执行顺序
            └── markName         // 标记名: assemblyTypeCode-businessTypeCode-order
```

**核心能力：**

1. **多阶段编排**: 支持按序执行多个业务单元，每个单元经历 build → ready → complete 生命周期
2. **自动推导**: `createForType(typeCode)`工厂方法，优先查找已注册Assembly，未找到则自动创建`DynamicAssembly`
3. **测试用例管理**: 内置测试用例的录制、回放、检查、复盘、重生成
4. **上下文共享**: 装配线内所有单元共享同一Assembly实例
5. **模式切换**: 通过`setRecordMode()`/`setCheckMode()`等方法切换运行模式
6. **SPI驱动**: 通过`TypeRegistry`动态解析业务类型和装配线类型

------

## 三、Pipeline 编排引擎 (pipeline包)

### 3.1 BusinessContext - 上下文传递

```java
@Getter
@Builder
public class BusinessContext<T extends UserBusinessVO> {
    private T businessVo;              // 输入VO
    private BusinessAssembly assembly; // 装配线引用（非泛型）

    public static <T extends UserBusinessVO>
    BusinessContext<T> build(T businessVo, BusinessAssembly assembly) {
        // 如果assembly为null，使用BusinessEmptyAssembly兜底
        return BusinessContext.<T>builder()
                .businessVo(businessVo)
                .assembly(Objects.nonNull(assembly) ? assembly
                    : BusinessAssembly.createAssembly(BusinessEmptyAssembly.class))
                .build();
    }
}
```

**设计要点：**
- 携带输入VO和装配线引用贯穿整个Pipeline
- 空装配线兜底，避免NullPointerException
- Assembly使用基类类型（不再是泛型参数），简化消费方代码

### 3.2 BusinessAssemblyUnit - 装配单元

```java
public class BusinessAssemblyUnit<V, T, R> {
    private BusinessContext<R> businessContext;   // 上下文
    private T businessEntity;                     // 实体（ready后设置）
    private V businessDealVO;                     // 输出（complete后设置）
    private int order;                            // 执行顺序
}
```

**生命周期：**

```
doBuild() → ready(entity) → complete(dealVO)
  创建          就绪            完成
```

**标记名规则：** `assemblyTypeCode-businessTypeCode-order`，用于测试用例中标识每个装配单元的数据

### 3.3 BusinessEmptyAssembly - 空装配线

```java
public class BusinessEmptyAssembly extends BusinessAssembly {
    public static final String EMPTY_CODE = "empty";

    @Override
    public String getAssemblyTypeCode() {
        return EMPTY_CODE;
    }
}
```

当不需要装配线编排时（如单一业务处理），使用空装配线作为兜底。

### 3.4 DynamicAssembly - 动态装配线

```java
public class DynamicAssembly extends BusinessAssembly {
    private final String typeCode;

    public DynamicAssembly(String typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    public String getAssemblyTypeCode() {
        return typeCode;
    }
}
```

**设计要点：**
- 由`BusinessAssembly.createForType(typeCode)`自动创建，当TypeRegistry中未注册对应Assembly类型时使用
- 替代消费方编写空壳Assembly子类，消费方无需创建Assembly类
- 支持完整的build → ready → complete生命周期
- Facade只需通过`getAssemblyTypeCode()`声明类型编码即可

------

## 四、录制/回放系统 (record包) - 测试基础设施

### 4.1 @RecordAndReplay 注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordAndReplay {
    String value() default "";   // 方法标识（默认使用方法名）
    String mark() default "";    // 参数标记方法名（用于参数标准化）
}
```

**使用方式：**

- **标注在方法上**: 该方法的调用会被录制/回放
- **标注在类上**: 该类所有方法都会被录制/回放
- **value="ignore"**: 跳过录制/回放，直接执行

### 4.2 RecordAndReplayAspect - AOP切面

**切面逻辑（按模式分支）：**

```
┌─────────────┐
│ 方法被调用   │
└──────┬──────┘
       │
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

**参数标准化：**

```java
// 复杂对象转为Map进行比对
public Object getDefaultMark(Object o) {
    if (o == null || BeanUtil.isPrimitive(o.getClass())
        || o instanceof String || o instanceof Collection) {
        return o;
    }
    return jsonSerializer.toMap(jsonSerializer.toJson(o));
}
```

### 4.3 InteractionRecord - 交互记录

```java
public class InteractionRecord implements Serializable {
    private String methodName;       // 方法标识
    private List<Object> arguments;  // 输入参数
    private Object result;           // 返回值
}
```

**存储结构：**

```
BusinessEnv.records: Map<String, List<InteractionRecord>>
    │
    ├── "getNowTime" → [{methodName, args=[], result=1700000000000}]
    ├── "queryUser"  → [{methodName, args=[{userId: "123"}], result={...}}]
    └── "getConfig"  → [{methodName, args=["key1"], result="value1"},
                         {methodName, args=["key2"], result="value2"}]
```

### 4.4 六种运行模式

| 模式 | 枚举值 | 描述 | 外部交互 | 记录行为 |
|------|--------|------|----------|----------|
| LIVE | `实时` | 生产模式 | 正常执行 | 不记录 |
| RECORD | `记录` | 录制模式 | 正常执行 | 记录到records |
| REPLAY | `重播` | 回放模式 | 优先回放，找不到则执行 | 记录到records |
| CHECK | `检查` | 检查模式 | 严格回放，找不到则抛异常 | 记录到records |
| REVIEW | `复盘` | 复盘模式 | 同CHECK | 记录到records |
| REGENERATE | `重生成` | 重生成模式 | 同CHECK，完成后更新用例 | 记录到records |

------

## 五、测试用例管理 (testcase包)

### 5.1 TestCaseVO - 测试用例数据

```java
public class TestCaseVO implements Serializable {
    private Integer id;                // 唯一标识
    private String name;               // 测试用例名称
    private String description;        // 描述
    private String businessType;       // 业务类型编码
    private String businessName;       // 业务名称
    private String multiBusinessVO;    // JSON: Map<markName, BusinessVO>
    private String multiBusinessEnv;   // JSON: Map<markName, Map<key, List<InteractionRecord>>>
    private String multiBusinessDealVO;// JSON: Map<markName, BusinessDealVO>
}
```

**三个JSON字段的含义：**

- **multiBusinessVO**: 每个装配单元的输入数据快照
- **multiBusinessEnv**: 每个装配单元的外部交互记录（用于回放）
- **multiBusinessDealVO**: 每个装配单元的输出结果（用于比对）

### 5.2 TestCaseRunner - 测试执行器

```java
public class TestCaseRunner {
    private BusinessFacade<?, ?, ?> facade;              // 业务Facade实例
    private TestCasePersistenceService persistenceService; // 持久化SPI

    public TestCaseRunner(BusinessFacade<?, ?, ?> facade,
                          TestCasePersistenceService persistenceService) { ... }

    // 执行测试用例
    public BusinessAssembly run(BusinessMode mode, TestCaseVO testCaseVO) {
        BusinessAssembly assembly = BusinessAssembly.createForType(
            facade.getAssemblyTypeCode());
        assembly.doRunTestCaseViaFacade(mode, facade, testCaseVO, persistenceService);
        return assembly;
    }

    // 从执行结果中提取第一个BusinessVO
    public BusinessVO extractFirstBusinessVO(BusinessAssembly assembly) { ... }

    // 生成结果快照
    public TestCaseVO generateSnapshot(BusinessAssembly assembly) { ... }
}
```

### 5.3 TestCaseEngine - 测试引擎

**高层编排器，封装完整的测试用例生命周期：**

```java
public class TestCaseEngine {
    private TestCaseRunner runner;
    private TestCaseComparator comparator;
    private TestCasePersistenceService persistenceService;
    private JsonSerializer jsonSerializer;

    public TestCaseEngine(BusinessFacade<?, ?, ?> facade,
                          TestCasePersistenceService persistenceService,
                          JsonSerializer jsonSerializer) { ... }
}
```

**核心方法：**

| 方法 | 模式 | 描述 |
|------|------|------|
| `record(TestCaseVO)` | RECORD | 录制测试用例并持久化 |
| `check(Integer)` | CHECK | 按ID加载用例，严格验证 |
| `review(TestCaseVO)` | REVIEW | 使用提供的数据验证 |
| `replay(TestCaseVO)` | REPLAY | 回放，找不到记录时降级执行 |
| `regenerate(Integer, TestCaseVO)` | REGENERATE | 重新生成并更新用例 |

**典型使用流程：**

```
1. record()  → 录制生产环境的真实交互
2. check()   → 代码变更后验证行为一致性
3. replay()  → 本地调试，无需外部依赖
4. regenerate() → 业务逻辑变更后更新用例
```

### 5.4 TestCaseComparator - 结果比对

```java
public class TestCaseComparator {
    private JsonSerializer jsonSerializer;
    private ObjectCompareUtil objectCompareUtil;

    // 比对两个TestCaseVO的三个JSON字段
    public boolean equalsCase(TestCaseVO expected, TestCaseVO actual,
                              Map<String, String> inconsistencies) {
        boolean voEquals = equalsJsonField("multiBusinessVO",
                expected.getMultiBusinessVO(), actual.getMultiBusinessVO(), inconsistencies);
        boolean envEquals = equalsJsonField("multiBusinessEnv",
                expected.getMultiBusinessEnv(), actual.getMultiBusinessEnv(), inconsistencies);
        boolean dealEquals = equalsJsonField("multiBusinessDealVO",
                expected.getMultiBusinessDealVO(), actual.getMultiBusinessDealVO(), inconsistencies);
        return voEquals && envEquals && dealEquals;
    }
}
```

**比对特点：**
- 自动移除`identity`字段（UUID每次不同）
- 深度递归比对，支持Map、List、基本类型
- 记录不一致的路径和值到`inconsistencies` Map

------

## 六、SPI 扩展点设计 (spi包)

**设计哲学：** 核心框架依赖接口（`core.spi`），自动配置提供默认实现。消费方可通过声明自己的`@Bean`覆盖任意SPI实现。

### 6.1 TypeRegistry - 类型注册中心

```java
public interface TypeRegistry {
    void registerBusinessType(BusinessTypeIdentifier type);
    void registerAssemblyType(AssemblyTypeIdentifier type);
    BusinessTypeIdentifier resolveBusinessType(String code);
    AssemblyTypeIdentifier resolveAssemblyType(String code);
    Class<? extends UserBusinessVO> resolveVoClass(String businessTypeCode);
}
```

**作用：** 替代硬编码的枚举（如`BusinessEnum`、`AssemblyEnum`），实现运行时动态类型注册和解析。

### 6.2 JsonSerializer - JSON序列化

```java
public interface JsonSerializer {
    String toJson(Object object);
    <T> T fromJson(String json, Class<T> clazz);
    Map<String, Object> toMap(String json);
}
```

**默认实现：** `JacksonJsonSerializer`（基于Jackson ObjectMapper，忽略未知属性）

### 6.3 BeanProvider - Bean查找

```java
public interface BeanProvider {
    <T> T getBean(Class<T> clazz);
}
```

**默认实现：** `SpringBeanProvider`（委托给`ApplicationContext.getBean()`）

### 6.4 TestCasePersistenceService - 测试用例持久化

```java
public interface TestCasePersistenceService {
    void saveOrUpdate(TestCaseVO testCaseVO);
    TestCaseVO load(Integer id);
}
```

**消费方必须实现此接口**，提供测试用例的存储和加载能力（如数据库、文件系统等）。

### 6.5 类型标识接口

```java
// 业务类型标识
public interface BusinessTypeIdentifier {
    String getCode();                              // 唯一编码
    String getDescription();                       // 描述
    Class<? extends UserBusinessVO> getVoClass();  // 关联的VO类
}

// 便捷实现 — Java record，替代消费方编写枚举
public record SimpleBusinessType(
    String code,
    String description,
    Class<? extends UserBusinessVO> voClass
) implements BusinessTypeIdentifier {
    @Override public String getCode() { return code; }
    @Override public String getDescription() { return description; }
    @Override public Class<? extends UserBusinessVO> getVoClass() { return voClass; }
}

// 装配线类型标识（仅高级场景需要，DynamicAssembly已覆盖大多数场景）
public interface AssemblyTypeIdentifier {
    String getCode();                                    // 唯一编码
    String getDescription();                             // 描述
    Class<? extends BusinessAssembly> getAssemblyClass();// 关联的Assembly类
}

// 作用域标识
public interface ScopeIdentifier {
    String getCode();          // 唯一编码
    String getDescription();   // 描述
}
```

------

## 七、自动配置 (autoconfigure包)

### 7.1 AiFrameworkAutoConfiguration

```java
@AutoConfiguration
@EnableConfigurationProperties(AiFrameworkProperties.class)
public class AiFrameworkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JsonSerializer jsonSerializer() {
        return new JacksonJsonSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanProvider beanProvider(ApplicationContext context) {
        SpringBeanProvider provider = new SpringBeanProvider(context);
        BusinessHelper.configureBeanProvider(provider);  // 初始化框架
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeRegistry typeRegistry(
            JsonSerializer jsonSerializer,
            ObjectProvider<List<BusinessTypeIdentifier>> businessTypes,
            ObjectProvider<List<AssemblyTypeIdentifier>> assemblyTypes) {
        DefaultTypeRegistry registry = new DefaultTypeRegistry();
        // 自动收集所有 BusinessTypeIdentifier Bean 并注册
        businessTypes.ifAvailable(types ->
            types.forEach(registry::registerBusinessType));
        // 自动收集所有 AssemblyTypeIdentifier Bean 并注册
        assemblyTypes.ifAvailable(types ->
            types.forEach(registry::registerAssemblyType));
        BusinessAssembly.configure(registry, jsonSerializer);
        return registry;
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai-framework", name = "aspect-enabled",
                           havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public RecordAndReplayAspect recordAndReplayAspect(JsonSerializer jsonSerializer) {
        return new RecordAndReplayAspect(jsonSerializer);
    }
}
```

**关键设计：**
- 所有Bean都标注`@ConditionalOnMissingBean`，消费方可完全覆盖
- 初始化时自动配置框架的静态依赖（`BusinessHelper.configureBeanProvider()`、`BusinessAssembly.configure()`）
- **自动类型收集**：通过`ObjectProvider<List<BusinessTypeIdentifier>>`自动发现并注册所有业务类型Bean，消费方无需手动创建TypeConfig配置类
- AOP切面可通过配置开关控制

### 7.2 默认实现

| SPI接口 | 默认实现 | 说明 |
|---------|---------|------|
| `JsonSerializer` | `JacksonJsonSerializer` | 基于Jackson，忽略未知属性 |
| `BeanProvider` | `SpringBeanProvider` | 委托给ApplicationContext |
| `TypeRegistry` | `DefaultTypeRegistry` | 内存Map存储 |

### 7.3 配置属性

```yaml
ai-framework:
  record-enabled: true    # 是否启用录制功能
  aspect-enabled: true    # 是否启用AOP切面
```

**配置前缀：** `ai-framework`

------

## 八、异常体系 (exception包)

```
RuntimeException
    └── BusinessException (框架基础异常)
            ├── InterruptException (中断异常 - 终止当前业务流程)
            ├── SkipException (跳过异常 - 跳过当前操作，不影响整体)
            └── DegradeException (降级异常 - 触发降级逻辑)
```

**使用场景：**

| 异常类型 | 场景 | Facade处理 |
|---------|------|-----------|
| `InterruptException` | 严重错误，必须终止 | 向上抛出 |
| `SkipException` | 当前单元可跳过 | 返回null，继续下一单元 |
| `DegradeException` | 需要降级处理 | 执行`degrade()`回调，返回null |

**InterruptException特殊能力：**

```java
// 递归解包嵌套的InterruptException
public static InterruptException getInterruptWarp(Throwable e) {
    if (e instanceof InterruptException) return (InterruptException) e;
    if (e.getCause() != null) return getInterruptWarp(e.getCause());
    return null;
}

// 从异常链中提取中断消息
public static String getInterruptMessage(Throwable e, String defaultMsg) { ... }
```

**DegradeException降级机制：**

```java
public class DegradeException extends BusinessException {
    private Runnable degrade;  // 降级处理器

    public void degrade() {
        try {
            degrade.run();
        } catch (Exception e) {
            throw new InterruptException("降级处理失败", e);
        }
    }
}
```

------

## 九、工具类 (util包)

**CacheInvoke - 反射方法缓存：**

```java
public class CacheInvoke {
    private static final Map<String, Method> cacheMethod = new HashMap<>();

    // 缓存Method对象，避免重复反射查找
    public Object cacheInvoke(Object target, String methodName,
                              List<Class<?>> paramTypes, Object... args) {
        String key = target.getClass().getName() + "#" + methodName;
        Method method = cacheMethod.computeIfAbsent(key, k ->
            findMethod(target.getClass(), methodName, paramTypes));
        return method.invoke(target, args);
    }
}
```

**LazyFutureTask / BusinessFutureTask - 延迟执行：**

```java
// 延迟到get()时才执行
public class LazyFutureTask<V> extends FutureTask<V> {
    private final AtomicBoolean started = new AtomicBoolean(false);

    public V get() {
        start();  // 首次get时触发执行
        return super.get();
    }
}

// 自动注册到BusinessDealVO的异步任务
public class BusinessFutureTask<V> extends LazyFutureTask<V> {
    public BusinessFutureTask(BusinessDealVO<?> source, Callable<V> callable) {
        super(callable);
        source.addFutureTask(this);  // 自动注册
    }
}
```

**ObjectCompareUtil - 深度对象比对：**

```java
public class ObjectCompareUtil {
    // 深度递归比对，记录不一致路径
    public boolean objectEquals(Object o1, Object o2, String path,
                                Map<String, String> inconsistencies) {
        // 处理: null、基本类型、String、Collection、Map、复杂对象
    }
}
```

**BeanUtil - 类型工具：**

```java
public class BeanUtil {
    // 判断是否为基本类型或包装类型
    public static boolean isPrimitive(Class<?> clazz) { ... }
    // 获取包装类型
    public static Class<?> getWrapperClass(Class<?> primitiveClass) { ... }
}
```

------

## 十、设计模式与编程范式

### 10.1 使用的设计模式

| 设计模式 | 应用位置 | 说明 |
|---------|---------|------|
| **模板方法** | `BusinessFacade.process()` | 定义 build→process→buildVO→afterProcess 标准流程，子类实现`doProcess()` |
| **门面模式** | `BusinessFacade` | 封装复杂的Pipeline执行流程，对外提供简单的`process()`入口 |
| **组合模式** | `BusinessAssembly` + `BusinessAssemblyUnit` | 装配线由多个装配单元组合而成 |
| **策略模式** | `BusinessMode` + `RecordAndReplayAspect` | 根据运行模式切换录制/回放/检查策略 |
| **SPI模式** | `core.spi` 包 | 核心依赖接口，自动配置提供默认实现，消费方可替换 |
| **原型模式** | `BusinessEnv` (`@Scope(PROTOTYPE)`) | 每次获取都是新实例，保证环境隔离 |
| **空对象模式** | `BusinessEmptyAssembly` | 空装配线兜底，避免null判断 |
| **观察者模式** | `BusinessFutureTask` | 自动注册到`BusinessDealVO`的任务列表 |
| **代理模式** | `RecordAndReplayAspect` (AOP) | 透明拦截方法调用，实现录制/回放 |

### 10.2 泛型编程

**三层泛型约束：**

```java
BusinessFacade<
    V extends UserBusinessDealVO<T>,    // 输出VO，绑定到实体
    T extends BusinessEntity<?>,         // 实体
    R extends UserBusinessVO             // 输入VO
>
```

**泛型带来的好处：**

1. **编译期类型检查**: `doProcess(T, R)`的参数类型由编译器保证正确
2. **IDE代码提示**: Entity和VO类的方法完整自动补全
3. **重构安全**: 编译器保护，重命名不会遗漏
4. **消除强转**: `process(R)`返回精确的V类型

**简化思路：** 原先5个泛型参数（V, T, O, R, A），现精简为3个。Assembly和Helper通过`getAssemblyTypeCode()`和`getBusinessHelper()`方法替代泛型约束，在保留核心类型安全的同时大幅降低消费方代码量。

### 10.3 依赖注入模式

**框架采用双重依赖注入策略：**

1. **Spring DI**: 自动配置层通过`@Bean`注入SPI实现
2. **静态配置**: 核心层通过静态方法配置SPI（`BusinessHelper.configureBeanProvider()`、`BusinessAssembly.configure()`）

```java
// 自动配置时初始化
@Bean
public BeanProvider beanProvider(ApplicationContext context) {
    SpringBeanProvider provider = new SpringBeanProvider(context);
    BusinessHelper.configureBeanProvider(provider);  // 静态注入
    return provider;
}
```

**设计原因：** 核心层（`BusinessHelper`、`BusinessAssembly`）不依赖Spring，通过静态方法接收SPI实现，保持框架的独立性。

------

## 十一、代码示例

### 11.1 注册业务类型

**方式一：使用 SimpleBusinessType（推荐）**

```java
@Configuration
public class MyConfig {
    @Bean
    public BusinessTypeIdentifier orderType() {
        return new SimpleBusinessType("order", "订单处理", OrderVO.class);
    }

    @Bean
    public BusinessTypeIdentifier paymentType() {
        return new SimpleBusinessType("payment", "支付处理", PaymentVO.class);
    }
}
```

框架的`AiFrameworkAutoConfiguration`会通过`ObjectProvider`自动收集所有`BusinessTypeIdentifier` Bean并注册到`TypeRegistry`。无需手动调用`registry.register()`。

**方式二：使用枚举（复杂场景）**

```java
public enum MyBusinessType implements BusinessTypeIdentifier {
    ORDER("order", "订单处理", OrderVO.class),
    PAYMENT("payment", "支付处理", PaymentVO.class);

    private final String code;
    private final String description;
    private final Class<? extends UserBusinessVO> voClass;

    // getter methods...
}

// 枚举需要逐个注册为Bean
@Bean public BusinessTypeIdentifier orderType() { return MyBusinessType.ORDER; }
@Bean public BusinessTypeIdentifier paymentType() { return MyBusinessType.PAYMENT; }
```

### 11.2 实现业务实体与门面

**Step 1: 定义输入VO**

```java
@Getter
@Setter
public class OrderVO extends UserBusinessVO {
    private String orderId;
    private BigDecimal amount;
    private String productName;

    @Override
    public BusinessTypeIdentifier getBusinessType() {
        return new SimpleBusinessType("order", "订单处理", OrderVO.class);
    }
}
```

**Step 2: 定义输出DealVO**

```java
@Getter
public class OrderDealVO extends UserBusinessDealVO<OrderEntity> {
    private String orderStatus;
    private BigDecimal finalAmount;

    public OrderDealVO(Business business) {
        super(business);
    }

    @Override
    public <V extends BusinessDealVO<OrderEntity>> V doBuild(OrderEntity entity) {
        this.orderStatus = entity.getOrderStatus();
        this.finalAmount = entity.getFinalAmount();
        return (V) this;
    }
}
```

**Step 3: 实现Env（标注@RecordAndReplay的外部交互）**

```java
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RecordAndReplay
public class OrderEnv extends BusinessEnv {

    @RecordAndReplay("queryInventory")
    public int queryInventory(String productId) {
        // 实际调用库存服务
        return inventoryService.getStock(productId);
    }

    @RecordAndReplay("queryDiscount")
    public BigDecimal queryDiscount(String userId) {
        // 实际调用优惠服务
        return discountService.getUserDiscount(userId);
    }
}
```

**Step 4: 实现Entity — 使用 getEnv() 快捷方法 + @AfterProcess 注解**

```java
@Getter
public class OrderEntity extends BusinessEntity<BusinessHelper<OrderVO>> {
    private String orderStatus;
    private BigDecimal finalAmount;

    public OrderEntity(BusinessHelper<OrderVO> helper) {
        super(helper);
    }

    // doProcess(T, R) 中 VO 直接传入，无需从 Helper 长链路获取
    public void processOrder(OrderVO vo) {
        // getEnv() 快捷方法，无需 getBusinessHelper().getBusinessEnv(...)
        int stock = getEnv(OrderEnv.class).queryInventory(vo.getProductName());
        if (stock <= 0) {
            throw new SkipException("库存不足");
        }
        BigDecimal discount = getEnv(OrderEnv.class).queryDiscount(vo.getUserId());
        this.finalAmount = vo.getAmount().multiply(discount);
        this.orderStatus = "CONFIRMED";
    }

    // @AfterProcess 注解替代 Helper.saveDB()，无需自定义Helper
    @AfterProcess(AfterProcess.Phase.SAVE_DB)
    public void save() {
        // 持久化订单数据
        orderRepository.save(buildOrderEntity());
    }

    @AfterProcess(AfterProcess.Phase.DEL_REDIS)
    public void clearCache() {
        // 清除相关缓存
        redisTemplate.delete("order:" + getUserId());
    }

    @AfterProcess(AfterProcess.Phase.FINISH)
    public void onFinish() {
        // 发送通知
        eventPublisher.publishEvent(new OrderCompleteEvent(getUserId()));
    }
}
```

**Step 5: 实现Facade — 3个泛型，无需Assembly参数**

```java
@Component
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {

    @Override
    public String getAssemblyTypeCode() {
        return "order";
    }

    // VO直接传入doProcess，使用方便
    @Override
    public void doProcess(OrderEntity entity, OrderVO vo) {
        entity.processOrder(vo);
    }
}
```

**使用：**

```java
@Service
public class OrderService {
    @Autowired
    private OrderFacade orderFacade;

    public OrderDealVO handle(OrderVO orderVO) {
        return orderFacade.process(orderVO);  // 一行调用
    }
}
```

### 11.3 录制与回放测试用例

**创建TestCaseEngine：**

```java
// 直接传入Facade，无需反射的 (service, methodName) 模式
TestCaseEngine engine = new TestCaseEngine(
    orderFacade,           // BusinessFacade实例
    persistenceService,    // 持久化SPI
    jsonSerializer         // JSON序列化
);
```

**录制测试用例：**

```java
@SpringBootTest
public class OrderIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private TestCasePersistenceService persistenceService;

    @Autowired
    private JsonSerializer jsonSerializer;

    @Test
    public void testRecordOrderFlow() {
        TestCaseEngine engine = new TestCaseEngine(
            orderFacade, persistenceService, jsonSerializer);

        // 构造测试用例输入
        TestCaseVO testCase = new TestCaseVO();
        testCase.setName("order_test_001");

        // 录制（自动执行并记录所有@RecordAndReplay方法）
        TestCaseVO recorded = engine.record(testCase);

        assertNotNull(recorded);
        assertNotNull(recorded.getMultiBusinessVO());
    }
}
```

**回放验证：**

```java
@Test
public void testReplayOrderFlow() {
    TestCaseEngine engine = new TestCaseEngine(
        orderFacade, persistenceService, jsonSerializer);

    // CHECK模式 - 严格验证
    boolean checkResult = engine.check(1);  // 按ID加载并验证
    assertTrue(checkResult);

    // REPLAY模式 - 回放（无需外部依赖）
    TestCaseVO testCase = persistenceService.load(1);
    TestCaseVO replayResult = engine.replay(testCase);
    assertNotNull(replayResult);
}
```

### 11.4 自定义SPI实现

**自定义JsonSerializer（如使用Gson）：**

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
            return gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        }
    };
}
```

**自定义TestCasePersistenceService（如使用数据库）：**

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

### 11.5 高级用法：自定义Helper与Assembly

大多数场景下，使用框架默认的`BusinessHelper<R>`和`DynamicAssembly`即可。当需要在Helper中封装复杂的共享逻辑时，可以自定义：

**自定义Helper（复杂场景）：**

```java
public class OrderHelper extends BusinessHelper<OrderVO> {
    private OrderEnv orderEnv;

    public OrderEnv getOrderEnv() {
        if (orderEnv == null) {
            orderEnv = getBusinessEnv(OrderEnv.class);
        }
        return orderEnv;
    }

    // 也可以在Helper中定义生命周期钩子（与Entity上的@AfterProcess二选一）
    @Override
    public void saveDB() {
        orderRepository.save(buildOrderEntity());
    }
}
```

**Facade覆写getBusinessHelper（协变返回类型）：**

```java
@Component
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {

    @Autowired
    private OrderHelper orderHelper;

    @Override
    public String getAssemblyTypeCode() { return "order"; }

    // Java支持协变返回类型，可返回具体子类
    @Override
    public OrderHelper getBusinessHelper() {
        return orderHelper;
    }

    @Override
    public void doProcess(OrderEntity entity, OrderVO vo) {
        entity.processOrder(vo);
    }
}
```

**自定义Assembly（多单元编排场景）：**

仅当需要多个业务单元的复杂编排时才需要自定义Assembly。需同时注册`AssemblyTypeIdentifier`：

```java
public class OrderFlowAssembly extends BusinessAssembly {
    @Override
    public String getAssemblyTypeCode() { return "order-flow"; }

    // 自定义装配单元构建（CacheInvoke反射调用）
    public BusinessAssemblyUnit<OrderDealVO, OrderEntity, OrderVO>
    doBuild(OrderDealVO v, OrderEntity t, OrderVO r) {
        return BusinessAssemblyUnit.doBuild(v, t, r, this);
    }
}

// 注册Assembly类型
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

------

## 十二、最佳实践

### 12.1 开发规范

#### 12.1.1 命名规范

```
VO命名:     XxxVO (输入)、XxxDealVO (输出)
Entity命名: XxxEntity
Facade命名: XxxFacade
Helper命名: XxxHelper（可选，简单场景无需自定义）
Env命名:    XxxEnv
Assembly命名: XxxAssembly（可选，DynamicAssembly自动推导）
```

#### 12.1.2 Env方法规范

```java
// ✅ 正确：标注@RecordAndReplay，方法名有意义
@RecordAndReplay("queryUserInfo")
public UserInfo queryUserInfo(String userId) {
    return userService.getById(userId);
}

// ❌ 错误：未标注注解，无法录制/回放
public UserInfo queryUserInfo(String userId) {
    return userService.getById(userId);
}
```

#### 12.1.3 异常处理规范

```java
// ✅ 正确：使用框架异常体系
if (stock <= 0) {
    throw new SkipException("库存不足，跳过订单处理");
}

// ✅ 正确：降级处理
if (externalServiceDown) {
    throw new DegradeException("外部服务不可用", () -> {
        // 降级逻辑：使用缓存数据
        return cachedResult;
    });
}

// ❌ 错误：直接抛RuntimeException
throw new RuntimeException("处理失败");
```

#### 12.1.4 生命周期钩子

**方式一：在Entity上使用@AfterProcess注解（推荐）**

```java
// ✅ 推荐：注解驱动，无需自定义Helper
@AfterProcess(AfterProcess.Phase.SAVE_DB)
public void save() {
    if (needUpdate) {
        orderRepository.save(orderEntity);
        orderDetailRepository.saveAll(details);
    }
}

@AfterProcess(AfterProcess.Phase.DEL_REDIS)
public void clearCache() {
    redisTemplate.delete("order:" + getUserId());
    redisTemplate.delete("cart:" + getUserId());
}

@AfterProcess(AfterProcess.Phase.FINISH)
public void onComplete() {
    eventPublisher.publishEvent(new OrderCompleteEvent(orderId));
}
```

**方式二：在Helper中覆写方法（兼容旧模式）**

```java
// ✅ 也可以：在自定义Helper中覆写
@Override
public void saveDB() {
    if (needUpdate) {
        orderRepository.save(orderEntity);
    }
}

@Override
public void delRedis() {
    redisTemplate.delete("order:" + getUserId());
}

@Override
public void finish() {
    eventPublisher.publishEvent(new OrderCompleteEvent(orderId));
}
```

> 注意：如果Entity上有`@AfterProcess`注解，将优先执行注解方法，不再调用Helper的对应方法。两种方式不要在同一Phase上混用。

### 12.2 测试最佳实践

#### 12.2.1 录制测试用例

```java
// 使用TestCaseEngine直接录制，传入Facade即可
TestCaseEngine engine = new TestCaseEngine(orderFacade, persistenceService, jsonSerializer);
TestCaseVO testCase = new TestCaseVO();
testCase.setName("test_case_name");
TestCaseVO recorded = engine.record(testCase);
// 测试用例自动保存到持久化存储
// 包含：输入数据、外部交互记录、输出结果
```

#### 12.2.2 回放验证

```java
// CHECK模式：严格验证，所有外部交互必须有记录
engine.check(testCaseId);

// REPLAY模式：宽松回放，找不到记录时降级执行
engine.replay(testCaseVO);

// REVIEW模式：使用提供的数据验证
engine.review(testCaseVO);
```

#### 12.2.3 测试用例更新

```java
// 业务逻辑变更后，使用REGENERATE模式更新用例
engine.regenerate(testCaseId, newTestCaseVO);
```

### 12.3 故障排查

#### 12.3.1 日志追踪

```java
// 使用identity追踪完整链路
log.info("业务处理开始，identity={}, userId={}",
    business.getIdentity(), userId);

// Assembly也有独立identity
log.info("装配线创建，assemblyIdentity={}, mode={}",
    assembly.getIdentity(), assembly.getMode());
```

#### 12.3.2 测试用例重现

```
1. 根据日志找到出问题的请求
2. 在测试环境录制相同输入的测试用例
3. 使用REPLAY模式在本地重现问题
4. 修复后使用CHECK模式验证
5. 使用REGENERATE模式更新测试用例
```

#### 12.3.3 常见错误

```java
// 错误：InterruptException("测试用例数据异常，缺少 businessType 字段")
// 原因：测试用例JSON中缺少businessType
// 解决：检查TestCaseVO的multiBusinessVO字段

// 错误：InterruptException("未找到方法xxx的记录")
// 原因：CHECK模式下，covers中没有对应方法的记录
// 解决：重新录制测试用例，或切换到REPLAY模式

// 错误：InterruptException("组合线数据注入中断")
// 原因：assembly.build()失败，通常是类型注册问题
// 解决：检查TypeRegistry中是否注册了对应的BusinessType和AssemblyType
```

------

## 十三、常见问题FAQ

### 13.1 架构相关

**Q: 为什么从五层泛型简化到三层？**

A: 原先的五层泛型（V, T, O, R, A）虽然提供了完整的编译期类型安全，但消费方需要创建约10个类才能实现一个业务流程。分析发现：
- O (Helper) 和 A (Assembly) 的编译期约束实际价值有限（Assembly子类通常为空壳，Helper的类型安全通过协变返回类型保持）
- 保留V、T、R三个泛型覆盖了最核心的类型安全场景：`doProcess(T, R)`参数类型、`process(R)`返回类型V
- 简化后消费方所需类从10个降到4~5个

**Q: DynamicAssembly和自定义Assembly的区别？**

A:
- **DynamicAssembly**: 框架自动创建，适用于单一业务处理的大多数场景。消费方无需编写Assembly代码。
- **自定义Assembly**: 仅当需要多个业务单元的复杂编排（如多阶段Pipeline）时才需要。需要注册`AssemblyTypeIdentifier`。

**Q: BusinessAssembly和BusinessFacade的区别是什么？**

A:
- **BusinessFacade**: 单个业务实体的处理门面，定义了 `build → process → buildVO → afterProcess` 的标准流程
- **BusinessAssembly**: 多个业务实体的编排器，按顺序执行多个Facade，并管理测试用例

**Q: 为什么核心层使用静态方法配置SPI，而不是Spring注入？**

A: 核心层（`BusinessHelper`、`BusinessAssembly`）设计为不依赖Spring容器。通过静态方法接收SPI实现，使得：
- 框架核心可以在非Spring环境中使用
- 减少对Spring的耦合
- 自动配置层负责桥接Spring和核心层

**Q: 为什么BusinessEnv使用原型作用域？**

A:
- 每次业务处理需要独立的环境实例
- 避免多线程共享状态
- 每个实例有独立的`nowTime`、`records`、`covers`
- 保证录制/回放的隔离性

### 13.2 测试相关

**Q: 测试用例录制的原理是什么？**

A:
1. **AOP拦截**: `@RecordAndReplay`注解标记的方法会被`RecordAndReplayAspect`拦截
2. **参数标准化**: 将复杂对象转为Map，便于比对
3. **记录交互**: 保存方法名、参数、返回值到`InteractionRecord`
4. **序列化存储**: 将所有记录序列化为JSON，通过`TestCasePersistenceService`持久化

**Q: 测试用例录制会影响性能吗？**

A:
- **LIVE模式**: 无影响，切面直接放行
- **RECORD模式**: 轻微影响（序列化和存储），仅在测试环境使用
- **REPLAY模式**: 性能更好（不调用外部服务，直接返回记录结果）

**Q: 测试用例版本兼容怎么处理？**

A:
- 如果类结构变化（字段增删），旧用例可能不可用
- 建议使用REGENERATE模式重新生成
- 或者删除旧用例，重新录制新用例
- `JacksonJsonSerializer`配置了忽略未知属性，可容忍部分字段变化

### 13.3 扩展相关

**Q: 如何添加新的业务类型？**

A: 最少4~5个类即可：
1. 定义输入VO（继承`UserBusinessVO`）
2. 定义输出DealVO（继承`UserBusinessDealVO`）
3. 实现Env（继承`BusinessEnv`，标注`@RecordAndReplay`）
4. 实现Entity（继承`BusinessEntity`，使用`getEnv()`和`@AfterProcess`）
5. 实现Facade（继承`BusinessFacade<V,T,R>`，实现`getAssemblyTypeCode()`和`doProcess(T,R)`）
6. 注册类型：声明`@Bean public BusinessTypeIdentifier xxx() { return new SimpleBusinessType(...); }`

无需创建：Helper子类（简单场景）、Assembly子类、TypeConfig配置类、AssemblyType枚举。

**Q: 如何替换默认的JSON序列化？**

A: 声明自己的`@Bean`即可覆盖默认实现：
```java
@Bean
public JsonSerializer jsonSerializer() {
    return new MyCustomJsonSerializer();
}
```

**Q: 如何在非Spring环境中使用框架？**

A: 手动配置SPI：
```java
BusinessHelper.configureBeanProvider(myBeanProvider);
BusinessAssembly.configure(myTypeRegistry, myJsonSerializer);
```

------

## 十四、总结

### 14.1 核心优势

1. **架构清晰**: 三层架构（消费层、核心层、自动配置层），职责明确
2. **API简洁**: 3个泛型参数，消费方最少4~5个类即可实现完整业务流程
3. **抽象优雅**: 泛型约束覆盖核心类型安全，注解驱动生命周期
4. **扩展性强**: SPI驱动，所有核心依赖可替换
5. **可测试性好**: 内置录制/回放测试基础设施，支持六种运行模式
6. **低侵入性**: Spring Boot Starter方式接入，`@ConditionalOnMissingBean`全面覆盖
7. **环境无关**: 核心层不依赖Spring，可在任意Java环境使用
8. **自动推导**: Assembly自动创建、类型自动收集，减少胶水代码

### 14.2 学习路径

1. **入门阶段**: 理解`BusinessFacade`的模板方法流程（process(R) → doProcess(T, R) → buildVO）
2. **进阶阶段**: 掌握`@AfterProcess`注解、`getEnv()`快捷方法、`SimpleBusinessType`类型注册
3. **高级阶段**: 理解录制/回放机制和`TestCaseEngine`测试引擎
4. **专家阶段**: 自定义Helper、Assembly、SPI实现，扩展框架能力

### 14.3 注意事项

1. 所有外部交互方法必须标注`@RecordAndReplay`，否则无法录制/回放
2. `BusinessEnv`必须使用`@Scope(SCOPE_PROTOTYPE)`，保证实例隔离
3. 消费方必须实现`TestCasePersistenceService`才能使用测试用例功能
4. 业务类型需注册为`@Bean`（`BusinessTypeIdentifier`），框架自动收集
5. 使用框架异常体系（`InterruptException`/`SkipException`/`DegradeException`），不要直接抛`RuntimeException`
6. Entity上的`@AfterProcess`注解与Helper的生命周期方法在同一Phase上不要混用
7. `doProcess(T, R)` 中VO直接传入，无需通过`helper.getContext().getVo()`获取

------

**技术栈**: Java 21 + Spring Boot 4.0.2 + Gradle 8.14 + Spring AOP + Jackson + Lombok
**包名**: `com.tianrenservice.ai_framework_spring`
**配置前缀**: `ai-framework`
