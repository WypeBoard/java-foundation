# logging/core

A lightweight trace-logging utility for plain Java services. It wraps method execution and emits consistent entry, exit, timing, and exception logs through SLF4J, without requiring Spring AOP.

Use this module when you want explicit, code-level tracing (`trace(...)`) and keep full control over where tracing is applied.

---

## What's in here

### `TraceLogger`

Creates a logger wrapper and traces method execution.

```java
public class OrderService {

    private static final TraceLogger TRACE =
            TraceLogger.forClass(OrderService.class);

    public Order createOrder(CreateOrderRequest req) throws Exception {
        return TRACE.trace(
                "createOrder",
                TraceLoggingOptions.defaults(),
                () -> doCreateOrder(req),
                req
        );
    }
}
```

Key behavior:
- Logs entry and exit at the configured level.
- Logs execution time when enabled.
- Logs exceptions at `ERROR` and re-throws them.
- Skips all tracing when `setEnabled(false)` is used.
- Respects the underlying logger level (for example, `DEBUG` trace options do not emit when logger is at `INFO`).

Also supports void methods:

```java
TRACE.traceVoid("rebuildIndex", TraceLoggingOptions.defaults(), () -> {
    indexService.rebuild();
});
```

### `TraceLoggingOptions`

Per-invocation configuration for trace logging.

```java
var opts = TraceLoggingOptions.builder()
        .level(Level.DEBUG)
        .logArgs(true)
        .logResult(false)
        .logExecutionTime(true)
        .maxLength(1000)
        .fieldsToMask(Set.of("password", "cardNumber"))
        .build();
```

`TraceLoggingOptions.defaults()` values:

| Option | Default | Description |
|---|---|---|
| `level` | `INFO` | SLF4J level for entry/exit logs |
| `logArgs` | `true` | Include method arguments in entry log |
| `logResult` | `true` | Include return value in exit log |
| `logExecutionTime` | `true` | Append execution time as `[Nms]` |
| `maxLength` | `4000` | Max characters per logged value; longer values are truncated with `...[truncated]` |
| `fieldsToMask` | `{}` | Mask field values as `***` for `key=value` and `key:value` patterns (case-insensitive) |

### `TraceLoggerFormatter`

Low-level formatting helpers used by `TraceLogger`.

Most users will not call this directly, but it is public to keep message/argument formatting testable and reusable.

---

## Exception logging

Exceptions are always emitted at `ERROR`, regardless of `TraceLoggingOptions.level`, and then re-thrown unchanged.

This preserves normal error handling while still producing consistent trace output.

---

## Dependency

```kotlin
implementation("io.github.wypeboard.foundation:core-logging:0.1.0")
```

This module depends on `slf4j-api` as `compileOnly`, so your application should provide:
- an SLF4J binding (for example Logback), and
- your preferred logging configuration.

---

## When to use `logging/core` vs `logging/spring`

- Use `logging/core` when you want explicit tracing in plain Java or non-Spring code.
- Use `logging/spring` when you want annotation-driven tracing with Spring AOP (`@TraceLogging`).
