# utils/test

Internal helpers for building JUnit 5 extensions. Used by `date/test` and `database/test`. Useful if you are writing your own extensions and want the same annotation-resolution and field-reflection utilities.

---

## What's in here

### `ExtentionHelper`

Two static helpers:

**`retrieveAnnotationFromTestClasses`** — walks the `ExtensionContext` chain upward until it finds an annotation, resolving method-level then class-level then enclosing-class-level. This is how `@TimeAware` and `@CleanDatabase` support nested test classes and method-level overrides.

```java
Optional<CleanDatabase> annotation =
    ExtentionHelper.retrieveAnnotationFromTestClasses(CleanDatabase.class, context);
```

**`getFieldsFromInstanceHierarchy`** — collects all fields of a given type from every test instance in the context, including outer classes in nested test hierarchies.

```java
Set<InstantProvider> providers =
    ExtentionHelper.getFieldsFromInstanceHierarchy(context, InstantProvider.class);
```

---

## Dependency

```kotlin
testImplementation("io.github.wypeboard.foundation:test-utils:0.1.0")
```