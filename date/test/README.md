# date/test

JUnit 5 test helpers for time-dependent code. Provides a `@TimeAware` annotation that fixes the clock for each test and auto-stubs any `InstantProvider` mocks it finds in the test class.

Depends on [`date/core`](../core/README.md).

---

## What's in here

### `@TimeAware`

A composed annotation that wires a fixed `Clock` into your test and stubs any `InstantProvider` mocks declared as fields. Method-level annotations override class-level ones.

```java
@ExtendWith(MockitoExtension.class)
@TimeAware("2024-06-15")
class MyServiceTest {

    @Mock InstantProvider clock;    // auto-stubbed by @TimeAware

    @InjectMocks MyService service;

    @Test
    void returnsCorrectDate() {
        // clock.now() → 2024-06-15T00:00:00Z
        // clock.getDate() → 2024-06-15
        assertEquals(LocalDate.of(2024, 6, 15), service.today());
    }

    @Test
    @TimeAware("2024-12-31")        // method-level overrides class-level
    void overridesDateForThisTest() {
        assertEquals(LocalDate.of(2024, 12, 31), service.today());
    }
}
```

#### Accepted formats for `value`

| Format | Example | Resolved as |
|---|---|---|
| `yyyy-MM-dd` | `"2024-06-15"` | Midnight at start of day in the given zone |
| `yyyy-MM-dd HH:mm` | `"2024-06-15 10:30"` | That time in the given zone |
| `yyyy-MM-dd HH:mm:ss` | `"2024-06-15 10:30:45"` | That time in the given zone |
| *(omitted)* | — | Actual current time, captured once at test start |

#### All attributes

```java
@TimeAware(
    value   = "2024-06-15 10:30",   // fixed point in time
    zone    = "Europe/Copenhagen",   // defaults to UTC
    minDate = "2020-01-01T00:00:00Z", // defaults to Instant.MIN
    maxDate = "2030-01-01T00:00:00Z"  // defaults to Instant.MAX
)
```

`minDate` and `maxDate` are passed through to the stubbed `InstantProvider`. Omitting them defaults to `Instant.MIN`/`Instant.MAX` so bounds-checking code never accidentally fails in tests that don't care about bounds.

#### What gets stubbed

`@TimeAware` finds every field in the test class hierarchy whose type is `InstantProvider` and that is a Mockito mock or spy. It stubs:

- `now()` → the resolved `Instant`
- `zoneId()` → the resolved `ZoneId`
- `minDate()` / `maxDate()` → the resolved bounds
- `getDate()` → `LocalDate` derived from instant + zone
- `getDateTime()` → `LocalDateTime` derived from instant + zone

All stubs use `Mockito.lenient()` so they don't interfere with strict stubbing verification.

> **Extension ordering matters.** `MockitoExtension` or `SpringExtension` must be registered before `@TimeAware` so that mocks are created before the extension tries to stub them. `@TimeAware` uses `@ExtendWith(TimeAwareExtension.class)` internally. Adding Mockito via `@ExtendWith(MockitoExtension.class)` on the test class registers it first, which is correct.

---

### `DateAssertions`

Fluent assertion helpers for `Instant` and `ZonedDateTime`. Wrap `JUnit 5` assertions with messages that include the actual and expected values.

```java
DateAssertions.assertInstantEquals(expected, actual);
DateAssertions.assertBefore(reference, actual);        // actual.isBefore(reference)
DateAssertions.assertAfter(reference, actual);         // actual.isAfter(reference)
DateAssertions.assertSameDayInZone(a, b, zone);        // same calendar day
DateAssertions.assertWithinSeconds(expected, actual, 5); // within tolerance
DateAssertions.assertSameZone(expectedZone, zonedDateTime);
```

---

## Dependency

```kotlin
testImplementation("io.github.wypeboard.foundation:test-date:0.1.0")
```