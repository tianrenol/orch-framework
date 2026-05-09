# Repository Guidelines

## Project Structure & Module Organization

Multi-module Gradle project:
- `orch-framework-core/` — Pure Java orchestration engine. Zero Spring/AspectJ dependency. Contains pipeline, entity, record, SPI, testcase, util, and VO packages.
- `orch-framework-spring-boot-starter/` — Spring Boot auto-configuration, AOP aspect adapter, and default SPI implementations (Jackson, Spring context).
- Package: `com.tianrenservice.orch_framework` (underscores because Java packages can't have hyphens).

## Build, Test, and Development Commands
Use the Gradle wrapper to keep tooling consistent:
- `./gradlew build` (or `gradlew.bat build` on Windows): compile and run tests for all modules.
- `./gradlew :orch-framework-core:build`: build core module only.
- `./gradlew :orch-framework-spring-boot-starter:build`: build starter module only.
- `./gradlew test`: run all tests.
- `./gradlew test --tests "com.tianrenservice.orch_framework.SomeTest"`: run a single test class.
- `./gradlew clean`: remove build outputs.

## Coding Style & Naming Conventions
- Language: Java 21.
- Follow existing formatting: tabs for indentation and braces on the same line.
- Packages use underscores (e.g., `orch_framework`) since hyphens are invalid in Java packages.
- Class names use PascalCase; test classes typically end with `Tests`.
- No formatter or linter is configured, so keep changes consistent with nearby code.

## Testing Guidelines
- Test framework: JUnit 5 (Jupiter).
- Core module tests should not depend on Spring.
- Starter module tests use `spring-boot-starter-test`.
- Keep test method names descriptive.

## Commit & Pull Request Guidelines
- Commit messages are short, plain-English subjects.
- PRs should include a brief summary, steps to test, and any configuration changes.
- Link related issues when applicable.

## Security & Configuration Tips
- Do not commit secrets.
- Use environment variables or local overrides for credentials and API keys.
- If you add configuration keys, document them in the PR description.
