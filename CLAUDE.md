# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

Gradle multi-module project, Java 21. On Windows use `gradlew.bat` instead of `./gradlew`.

```bash
./gradlew build                                    # Build all modules
./gradlew :orch-framework-core:build               # Build core only
./gradlew :orch-framework-spring-boot-starter:build # Build starter only
./gradlew test                                      # Run all tests
./gradlew clean                                     # Clean
```

## Project Structure

Multi-module Gradle project with Spring-decoupled core:

```
orch-framework/                          (root)
├── orch-framework-core/                 (pure Java, zero Spring/AspectJ dependency)
└── orch-framework-spring-boot-starter/  (Spring Boot auto-configuration + AOP)
```

## Architecture

This is a reusable business process orchestration framework with recording/replay capabilities. The core module is framework-agnostic pure Java; the starter module provides Spring Boot integration.

**Package:** `com.tianrenservice.orch_framework`

### orch-framework-core (Pure Java)

**`core.pipeline`** — The pipeline orchestration engine:
- `BusinessFacade<V, T, R>` — Template-method entry point with 3 generics (V=DealVO, T=Entity, R=InputVO). Subclass and implement `doProcess(T, R)` to define business logic. Call `process(R)` to drive the full lifecycle: build → ready → process → complete.
- `BusinessAssembly` — Pipeline orchestrator that manages `BusinessAssemblyUnit` nodes. Handles test case modes (RECORD, REPLAY, CHECK, REVIEW, REGENERATE). Configured statically with `TypeRegistry` and `JsonSerializer`.
- `DynamicAssembly` — Default Assembly implementation auto-created by `BusinessAssembly.createForType()`. Consumers no longer need to write empty Assembly subclasses.
- `BusinessAssemblyUnit<V, T, R>` — Single execution node in a pipeline, holding context + entity + deal VO.
- `BusinessContext<T>` — Carries the input VO and assembly reference through the pipeline.

**`core.record`** — Recording/replay system:
- `@RecordAndReplay` annotation — Marks methods/classes for interaction recording.
- `RecordAndReplayHandler` — Pure Java handler containing all record/replay business logic. Defines `MethodInvoker` functional interface to decouple from AOP frameworks. The Spring starter provides an AspectJ-based adapter.
- `BusinessEnv` — Holds interaction records and manages mode-specific behavior. Pure POJO (no Spring annotations); registered as prototype bean by the starter.
- `InteractionRecord` — Stores method name, args, and result for a single interaction.

**`core.entity`** — Domain model base classes:
- `BusinessEntity<O>` — Core container for business logic, wraps a `BusinessHelper`. Provides `getEnv(Class)` shortcut for Env access and supports `@AfterProcess` annotation for lifecycle hooks directly on the Entity.
- `BusinessHelper<R>` — Manages context, env, and lifecycle hooks (saveDB, delRedis, finish). Uses `BeanProvider` SPI for dependency lookup. In simple scenarios, consumers can use the default `BusinessHelper<R>` without creating a subclass.

**`core.annotation`** — Lifecycle annotations:
- `@AfterProcess(Phase)` — Marks Entity methods as lifecycle hooks (SAVE_DB, DEL_REDIS, FINISH). Replaces the need to override Helper methods. Falls back to Helper if no annotation found.

**`core.spi`** — Extension points (all pure Java interfaces, replaceable via Spring `@Bean` or manual wiring):
- `TypeRegistry` — Registers and resolves business types and assembly types at runtime.
- `SimpleBusinessType` — Record implementing `BusinessTypeIdentifier`, replaces consumer-written enums for simple cases.
- `JsonSerializer` — JSON serialization contract.
- `BeanProvider` — Bean lookup abstraction (decouples from Spring ApplicationContext).
- `BusinessTypeIdentifier` / `AssemblyTypeIdentifier` / `ScopeIdentifier` — Type identity contracts.
- `TestCasePersistenceService` — Persistence for test cases.

**`core.constant`** — `BusinessMode` enum: LIVE, RECORD, REPLAY, CHECK, REVIEW, REGENERATE.

**`core.testcase`** — Test case management:
- `TestCaseEngine` — High-level test orchestrator. Constructor takes `BusinessFacade<?, ?, ?>` directly.
- `TestCaseRunner` — Executes test cases via `BusinessFacade`.

### orch-framework-spring-boot-starter (Spring Integration)

**`autoconfigure`** — Spring Boot auto-configuration:
- `OrchFrameworkAutoConfiguration` — Wires default SPI implementations (Jackson, Spring context, DefaultTypeRegistry) and initializes the framework. Auto-collects all `BusinessTypeIdentifier` and `AssemblyTypeIdentifier` beans via `ObjectProvider`. Registers `BusinessEnv` as prototype bean and `RecordAndReplayHandler`. All beans are `@ConditionalOnMissingBean` so consumers can override.
- `OrchFrameworkProperties` — Config prefix `orch-framework` with `record-enabled` and `aspect-enabled` flags.
- `RecordAndReplayAspect` — Thin AspectJ adapter that delegates to `RecordAndReplayHandler`. Adapts `ProceedingJoinPoint` to handler's `MethodInvoker` interface.
- `spi/` — Default SPI implementations: `DefaultTypeRegistry`, `JacksonJsonSerializer`, `SpringBeanProvider`.

### Key Patterns

- **SPI-driven extensibility**: Core depends on interfaces (`core.spi`), auto-configuration provides defaults. Consumers override by declaring their own `@Bean`.
- **Template method**: `BusinessFacade.process(R)` orchestrates the lifecycle; subclasses implement `doProcess(T, R)`.
- **Auto type registration**: Declare `BusinessTypeIdentifier` as `@Bean` and the framework auto-collects them. No manual `TypeConfig` class needed.
- **Annotation-driven lifecycle**: Use `@AfterProcess(Phase.SAVE_DB)` on Entity methods instead of overriding Helper. Helper is optional in simple scenarios.
- **Assembly auto-derivation**: `DynamicAssembly` is auto-created when no registered Assembly subclass exists. Consumers only need `getAssemblyTypeCode()` on Facade.
- **Handler/Adapter pattern for AOP**: `RecordAndReplayHandler` (core, pure Java) holds all logic; `RecordAndReplayAspect` (starter) is a thin AspectJ adapter. Non-Spring users can call the handler directly.
- **Reflective invocation via `CacheInvoke`**: Used throughout for dynamic method dispatch with caching.
- **Six business modes** control framework behavior — LIVE for production, RECORD to capture interactions, REPLAY/CHECK/REVIEW/REGENERATE for test case execution.

### Consumer Minimum Code (4-5 classes)

```java
// 1. VO
public class OrderVO extends UserBusinessVO { ... }

// 2. DealVO
public class OrderDealVO extends UserBusinessDealVO<OrderEntity> { ... }

// 3. Env — @Component @Scope on consumer's subclass (not inherited from base)
@Component @Scope(PROTOTYPE) @RecordAndReplay
public class OrderEnv extends BusinessEnv { ... }

// 4. Entity — uses getEnv() + @AfterProcess, no custom Helper needed
public class OrderEntity extends BusinessEntity<BusinessHelper<OrderVO>> {
    @AfterProcess(Phase.SAVE_DB) public void save() { ... }
}

// 5. Facade — 3 generics, default Helper
@Component
public class OrderFacade extends BusinessFacade<OrderDealVO, OrderEntity, OrderVO> {
    @Override public String getAssemblyTypeCode() { return "order"; }
    @Override public void doProcess(OrderEntity e, OrderVO vo) { ... }
}

// Type registration — just a @Bean
@Bean public BusinessTypeIdentifier orderType() {
    return new SimpleBusinessType("order", "Order Processing", OrderVO.class);
}
```

## Technology Stack

- Java 21 (Gradle toolchain)
- Gradle 8.14
- **orch-framework-core**: Pure Java + Lombok (compile-only) + SLF4J API
- **orch-framework-spring-boot-starter**: Spring Boot 4.0.2, Spring AOP + AspectJ, Jackson
- JUnit 5 for testing
