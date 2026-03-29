# date/spring

Spring Boot auto-configuration for `InstantProvider`. Wires a `Clock` bean and a fully configured `InstantProvider` implementation from application properties.

Depends on [`date/core`](../core/README.md).

---

## What's in here

### `InstantProviderImpl`

The production implementation of `InstantProvider`. Delegates `now()` to a Spring-managed `Clock` bean, and reads zone/bounds from `InstantProviderProperties`.

Spring Boot's auto-configuration registers both beans automatically — you just inject `InstantProvider` wherever you need it:

```java
@Service
public class BookingService {
    private final InstantProvider clock;

    public BookingService(InstantProvider clock) {
        this.clock = clock;
    }

    public boolean isAvailable(Interval slot) {
        return slot.contains(clock.now());
    }
}
```

### Configuration properties

All properties are under the `foundation.date` prefix.

| Property | Default | Description |
|---|---|---|
| `foundation.date.zone` | `Europe/Copenhagen` | The time zone used for `getDate()`, `getDateTime()`, `zonedNow()` |
| `foundation.date.min-date` | `1970-01-01T00:00:00Z` | Lower bound for `InstantProviderUtils.requireWithinBounds()` |
| `foundation.date.max-date` | `2099-12-31T23:59:59Z` | Upper bound |

```yaml
foundation:
  date:
    zone: UTC
    min-date: 2020-01-01T00:00:00Z
    max-date: 2030-12-31T23:59:59Z
```

### `DateConfig`

The `@Configuration` class that registers the `Clock` and `InstantProvider` beans. It is picked up by Spring Boot's auto-configuration mechanism — no `@Import` needed in your application.

---

## Dependency

```kotlin
implementation("io.github.wypeboard.foundation:spring-date:0.1.0")
```

Declare `spring-boot-autoconfigure` on your compile classpath (it is `compileOnly` in this module, so your application brings it):

```kotlin
implementation("org.springframework.boot:spring-boot-autoconfigure")
```

---

## Testing

In tests, swap the real provider for a mock and stub it with `@TimeAware`:

```java
@ExtendWith(MockitoExtension.class)
@TimeAware("2024-06-15")
class BookingServiceTest {

    @Mock InstantProvider clock;

    // @TimeAware stubs clock.now() → 2024-06-15T00:00:00Z automatically
}
```

See [`date/test`](../test/README.md) for full details.