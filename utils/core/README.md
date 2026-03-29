# utils/core

Small, dependency-free utilities used across the other modules. You can pull these in standalone if they are useful in isolation.

---

## What's in here

### `PredicateUtils`

Composes predicates from extractors and adds a throwing variant.

```java
// Filter by a property of T without an intermediate stream map
List<InstantProvider> mocks = providers.stream()
    .filter(by(details -> details.isMock(), Mockito::mockingDetails))
    .toList();

// Fail fast inside a stream filter with a clear message
providers.stream()
    .filter(throwIfNot(Objects::nonNull, "A supplied provider is null"))
    .forEach(...);
```

```java
// Signatures
<T, R> Predicate<T> by(Predicate<R> predicate, Function<T, R> extractor)
<T>    Predicate<T> throwIfNot(Predicate<T> predicate, String errorMessage)
```

### `StreamUtils`

Null-safe stream from a nullable collection.

```java
// Safe replacement for collection.stream() when the collection may be null
StreamUtils.ofNullable(nullableList).forEach(item -> ...);
```

---

## Dependency

```kotlin
implementation("io.github.wypeboard.foundation:core-utils:0.1.0")
```