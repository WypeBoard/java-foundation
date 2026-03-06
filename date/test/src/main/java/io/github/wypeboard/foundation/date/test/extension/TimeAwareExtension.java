package io.github.wypeboard.foundation.date.test.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * JUnit 5 extension backing {@link TimeAware}.
 * <p>
 * Lifecycle:
 * 1. beforeEach  — resolves annotation (method > class), builds fixed Clock, injects into Clock fields
 * 2. afterEach   — clears injected fields back to null (clean state between tests)
 */
public class TimeAwareExtension implements BeforeEachCallback, AfterEachCallback {

    private static final String DATE_ONLY_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern(DATE_ONLY_PATTERN);
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    // Store injected field names per test instance for clean afterEach
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TimeAwareExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Optional<TimeAware> annotation = resolveAnnotation(context);
        if (annotation.isEmpty()) {
            return;
        }

        Clock fixedClock = buildClock(annotation.get());
        injectClockFields(context, fixedClock);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        clearClockFields(context);
    }

    // ── Annotation resolution ────────────────────────────────────────────────

    private Optional<TimeAware> resolveAnnotation(ExtensionContext context) {
        // Method-level first
        return context.getTestMethod()
                .map(m -> m.getAnnotation(TimeAware.class))
                .or(() ->
                        // Fall back to class-level
                        context.getTestClass()
                                .map(c -> c.getAnnotation(TimeAware.class))
                );
    }

    // ── Clock construction ───────────────────────────────────────────────────

    private Clock buildClock(TimeAware annotation) {
        ZoneId zone = ZoneId.of(annotation.zone());
        String value = annotation.value();
        Instant fixed = parse(value, zone);
        return Clock.fixed(fixed, zone);
    }

    private Instant parse(String value, ZoneId zone) {
        // Try date+time first, then date only
        if (value.contains(":")) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(value, DATE_TIME_FMT);
                return ldt.atZone(zone).toInstant();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "@TimeAware value '%s' does not match pattern '%s'"
                                .formatted(value, DATE_TIME_PATTERN), e);
            }
        }

        try {
            LocalDate ld = LocalDate.parse(value, DATE_ONLY_FMT);
            return ld.atStartOfDay(zone).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "@TimeAware value '%s' does not match pattern '%s' or '%s'"
                            .formatted(value, DATE_ONLY_PATTERN, DATE_TIME_PATTERN), e);
        }
    }

    // ── Field injection ──────────────────────────────────────────────────────

    private void injectClockFields(ExtensionContext context, Clock clock) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Class<?> testClass = testInstance.getClass();

        for (Field field : allFields(testClass)) {
            if (field.getType().equals(Clock.class)) {
                field.setAccessible(true);
                field.set(testInstance, clock);
                // Track which fields we touched for cleanup
                context.getStore(NAMESPACE).put(field.getName(), field);
            }
        }
    }

    private void clearClockFields(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Class<?> testClass = testInstance.getClass();

        for (Field field : allFields(testClass)) {
            if (field.getType().equals(Clock.class)) {
                field.setAccessible(true);
                field.set(testInstance, null);
            }
        }
    }

    /**
     * Walk the class hierarchy to catch fields declared in superclasses.
     */
    private Iterable<Field> allFields(Class<?> clazz) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(java.util.Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}