# logging/spring

A single AOP aspect that logs method entry, exit, execution time, and exceptions — configured entirely through an annotation. No boilerplate logging code in your services.

Requires Spring Boot AOP and an SLF4J-compatible logger on the classpath.

---

## What's in here

### `@TraceLogging`

Annotate a method or a class. The aspect wraps the invocation and logs through the class's own logger (resolved via `LoggerFactory.getLogger(target.getClass())`), so log output appears under the correct logger name in your log configuration.

```java
@Service
public class OrderService {

    @TraceLogging
    public Order createOrder(CreateOrderRequest request) {
        // logs entry with args, exit with return value and elapsed time
    }
}
```

Class-level annotation applies to every method in the class:

```java
@TraceLogging(level = Level.DEBUG, logResult = false)
@Service
public class AuditService { ... }
```

#### All attributes

| Attribute | Default | Description |
|---|---|---|
| `level` | `INFO` | SLF4J log level for entry/exit messages |
| `logArgs` | `true` | Whether to include method arguments in the entry log |
| `logResult` | `true` | Whether to include the return value in the exit log |
| `logExecutionTime` | `true` | Whether to append `[Nms]` to the exit log |
| `maxLength` | `4000` | Max characters for any single logged value; longer values are truncated with `...[truncated]` |
| `fieldsToMask` | `{}` | Field names whose values are replaced with `***` in the logged output. Matches `key=value` and `"key":"value"` patterns (case-insensitive). |

#### Exception logging

Exceptions are always logged at `ERROR` regardless of the configured `level`, then re-thrown. The log includes the class name, method name, elapsed time, exception type, and message.

#### Masking sensitive fields

```java
@TraceLogging(fieldsToMask = {"password", "cardNumber"})
public void authenticate(AuthRequest request) { ... }
```

If `request.toString()` produces `AuthRequest{password=supersecret, cardNumber=4111111111111111}`, the log will show `AuthRequest{password=***, cardNumber=***}`.

---

## Setup

Register the aspect as a Spring bean. If you are using `@SpringBootApplication` with component scanning, add `LoggingConfig` to your imports or let the auto-configuration pick it up:

```java
@Configuration
@Import(LoggingConfig.class)
public class AppConfig { }
```

Or include the starter dependency if one is published — check the root README for current coordinates.

Make sure `spring-boot-starter-aop` is on your classpath:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-aop")
```

---

## Dependency

```kotlin
implementation("io.github.wypeboard.foundation:spring-logging:0.1.0")
```

`spring-boot-starter-aop` and `slf4j-api` are `compileOnly` in this module — your application brings them.