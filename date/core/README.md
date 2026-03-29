# date/core

Plain Java date and time abstractions. No framework dependencies.

---

## What's in here

### `InstantProvider`

An interface that abstracts the current point in time away from `Clock` and `Instant.now()`. Inject it into your services instead of calling `Instant.now()` directly — tests can then stub it without touching system state.

```java
public interface InstantProvider {
    Instant now();
    ZoneId zoneId();
    Instant minDate();
    Instant maxDate();

    default ZonedDateTime zonedNow() { ... }
    default LocalDate getDate() { ... }
    default LocalDateTime getDateTime() { ... }
}
```

`minDate()` and `maxDate()` define the valid operating range of the system. Utilities in `InstantProviderUtils` use these bounds for validation.

### `Interval`

An immutable, closed time interval `[from, to]`. Both endpoints are inclusive. Implements `Comparable<Interval>` so collections sort correctly.

```java
Interval booking = new Interval(checkIn, checkOut);

booking.contains(someInstant);      // true if within [from, to]
booking.overlaps(otherBooking);     // true if they share any point in time
booking.isAdjacentTo(nextBooking);  // true if one ends exactly where the other starts
```

For intervals with open ends, use the factory method — it substitutes `Instant.MIN` or `Instant.MAX`:

```java
Interval sinceEpoch = Interval.open(null, someEnd);   // [Instant.MIN, someEnd]
Interval untilEndOfTime = Interval.open(someStart, null); // [someStart, Instant.MAX]
```

### `IntervalUtils`

Static operations over collections of `Interval`.

```java
// Merge overlapping/adjacent intervals
List<Interval> merged = IntervalUtils.normalize(intervals);
// [1--3] [2--5] [7--9]  →  [1--5] [7--9]

// Split at every boundary point
List<Interval> atoms = IntervalUtils.atomize(intervals);
// [1--5] [3--7]  →  [1--3] [3--5] [5--7]

// Filter
List<Interval> hits = IntervalUtils.overlappingWith(intervals, target);
List<Interval> containing = IntervalUtils.containing(intervals, someInstant);

// Gap between two non-overlapping intervals
Optional<Interval> gap = IntervalUtils.gap(a, b);
```

### `InstantProviderUtils`

Validation and range helpers that work against an `InstantProvider`'s bounds:

```java
InstantProviderUtils.requireWithinBounds(provider, instant); // throws DateOutOfBoundsException

Interval remaining = InstantProviderUtils.remainingRange(provider); // now() → maxDate()
Interval elapsed   = InstantProviderUtils.elapsedRange(provider);   // minDate() → now()
Interval full      = InstantProviderUtils.fullRange(provider);       // minDate() → maxDate()
```

---

## Dependency

```kotlin
implementation("io.github.wypeboard.foundation:core-date:0.1.0")
```

No transitive dependencies beyond the JDK.

---

## Pairing

| You also want… | Add |
|---|---|
| Spring Boot wiring for `InstantProvider` | [`date/spring`](../spring/README.md) |
| Fixed-clock test support | [`date/test`](../test/README.md) |