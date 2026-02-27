# Orch Framework

## Project Overview

`orch-framework` is a reusable business process orchestration framework with recording/replay capabilities, organized as a multi-module Gradle project:

- **`orch-framework-core`** — Pure Java orchestration engine with zero Spring/AspectJ dependency. Can be used in any Java environment.
- **`orch-framework-spring-boot-starter`** — Spring Boot 4.0.2 auto-configuration that provides seamless integration via SPI defaults and AOP-based record/replay.

**Key Features:**

* **Framework-agnostic core:** The core module depends only on SLF4J API and Lombok (compile-only). All Spring/AspectJ/Jackson dependencies are isolated in the starter.
* **Auto-configuration:** The starter automatically configures default SPI implementations (JSON serialization, bean provisioning, type registration) and registers a thin AOP aspect for record/replay.
* **Extensibility (SPIs):** Developers can provide custom implementations for `JsonSerializer`, `BeanProvider`, `TypeRegistry`, and other services by defining their own Spring beans.
* **Interaction Recording & Replay:** `RecordAndReplayHandler` (core, pure Java) contains all record/replay business logic. The starter provides an AspectJ adapter, configurable via `orch-framework.aspect-enabled`.

**Technologies Used:**

* **Language:** Java 21
* **Build Tool:** Gradle 8.14 (multi-module)
* **Core module:** Pure Java + SLF4J API + Lombok (compile-only)
* **Starter module:** Spring Boot 4.0.2, Spring AOP + AspectJ, Jackson
* **Testing:** JUnit 5

**Architecture Highlights:**

The framework uses an SPI-driven architecture. The core module defines interfaces (`TypeRegistry`, `JsonSerializer`, `BeanProvider`, etc.) and the starter provides Spring-integrated defaults via `OrchFrameworkAutoConfiguration`. Key components like `BusinessHelper` and `BusinessAssembly` are initialized through static `configure()` methods, callable from any DI framework.

The record/replay system uses a Handler/Adapter pattern: `RecordAndReplayHandler` (core) holds all logic, while `RecordAndReplayAspect` (starter) is a thin AspectJ wrapper that delegates to the handler.

## Building and Running

### Building the Project

```bash
./gradlew build                                     # Build all modules
./gradlew :orch-framework-core:build                # Build core only
./gradlew :orch-framework-spring-boot-starter:build # Build starter only
```

### Running Tests

```bash
./gradlew test
```

## Development Conventions

* **Multi-module design:** Core module must remain Spring-free. All Spring dependencies belong in the starter.
* **SPI-driven:** Core services use interfaces from `core.spi`. The starter provides defaults via `@ConditionalOnMissingBean`.
* **Lombok:** Used for reducing boilerplate (getters, setters, constructors, logging).
* **Handler/Adapter pattern:** Record/replay logic is in `RecordAndReplayHandler` (pure Java). The AspectJ adapter in the starter wraps it for Spring AOP.
* **Package Structure:** `com.tianrenservice.orch_framework` with `core` (in core module) and `autoconfigure` (in starter module) sub-packages.
