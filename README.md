# foundation

A personal library of reusable Java building blocks — picked à la carte.

Each module is independent. Take what you need: plain Java utilities, Spring Boot enrichments, or test helpers. Nothing forces you to pull in the whole thing.

---

## Philosophy

This is not a framework. It is a collection of small, focused modules that solve recurring problems without imposing an architecture on you. If only the core date utilities fit your project, pull just that. If you want the full Spring Boot-wired `InstantProvider` with its test extension, pull all three date layers. Each module is published separately to GitHub Packages.

---

## Module overview

```
foundation
├── date/
│   ├── core        — Interval, InstantProvider interface, domain utilities
│   ├── spring      — Spring Boot auto-configuration for InstantProvider
│   └── test        — @TimeAware JUnit 5 extension for fixed-clock tests
│
├── database/
│   └── test        — @DatabaseTest + @CleanDatabase for Testcontainers Postgres
│
├── logging/
│   └── spring      — @TraceLogging AOP aspect for Spring Boot services
│
├── utils/
│   ├── core        — PredicateUtils, StreamUtils
│   └── test        — ExtentionHelper (shared JUnit 5 extension internals)
│
└── vcs/
    └── core/ado/pullrequest  — Azure DevOps pull request model objects
```

---

## Coordinates

All modules are published to GitHub Packages under the group `io.github.wypeboard.foundation`.

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/wypeboard/foundation")
        credentials {
            username = providers.gradleProperty("gpr.user").orElse(System.getenv("GITHUB_ACTOR")).get()
            password = providers.gradleProperty("gpr.key").orElse(System.getenv("GITHUB_TOKEN")).get()
        }
    }
}
```

Pick the modules you need:

```kotlin
dependencies {
    // Plain Java — no Spring required
    implementation("io.github.wypeboard.foundation:core-date:0.1.0")
    implementation("io.github.wypeboard.foundation:core-utils:0.1.0")

    // Spring Boot enrichments
    implementation("io.github.wypeboard.foundation:spring-date:0.1.0")
    implementation("io.github.wypeboard.foundation:spring-logging:0.1.0")

    // Test helpers (scope to testImplementation)
    testImplementation("io.github.wypeboard.foundation:test-date:0.1.0")
    testImplementation("io.github.wypeboard.foundation:test-database:0.1.0")
    testImplementation("io.github.wypeboard.foundation:test-utils:0.1.0")

    // VCS model
    implementation("io.github.wypeboard.foundation:core-vcs-ado-pullrequest:0.1.0")
}
```

---

## Modules

| Module | Artifact | What it gives you |
|---|---|---|
| [date/core](date/core/README.md) | `core-date` | `Interval`, `InstantProvider` interface, `IntervalUtils`, `InstantProviderUtils` |
| [date/spring](date/spring/README.md) | `spring-date` | Spring Boot auto-configuration for `InstantProvider`, config properties |
| [date/test](date/test/README.md) | `test-date` | `@TimeAware` JUnit 5 extension, `DateAssertions` |
| [database/test](database/test/README.md) | `test-database` | `@DatabaseTest`, `@CleanDatabase`, Testcontainers Postgres extension |
| [logging/spring](logging/spring/README.md) | `spring-logging` | `@TraceLogging` AOP aspect for method-level trace logging |
| [utils/core](utils/core/README.md) | `core-utils` | `PredicateUtils`, `StreamUtils` |
| [utils/test](utils/test/README.md) | `test-utils` | `ExtentionHelper` for building JUnit 5 extensions |
| [vcs/core/ado/pullrequest](vcs/core/ado/pullrequest/README.md) | `core-vcs-ado-pullrequest` | Jackson-mapped Azure DevOps pull request model |

---

## Requirements

- Java 21
- Gradle (wrapper included)
- Spring Boot 3.x (only for `spring-*` modules)
- Docker (only for `test-database`)

---

## Building locally

```bash
./gradlew build
```

Run tests for a single module:

```bash
./gradlew :core-date:test
```

---

## Module naming convention

The Gradle project names follow a `{type}-{feature}` pattern derived from the folder structure. A module at `date/spring/` becomes `:spring-date`. The `settings.gradle` does this automatically — no manual registration needed when adding new modules.