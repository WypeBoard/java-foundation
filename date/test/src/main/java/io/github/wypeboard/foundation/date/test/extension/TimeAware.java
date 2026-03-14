package io.github.wypeboard.foundation.date.test.extension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wires a fixed {@link java.time.Clock} and stubs any {@link io.github.wypeboard.foundation.date.core.provider.InstantProvider}
 * mocks found in the test class hierarchy.
 *
 * <p>All attributes are optional:
 * <ul>
 *   <li>Omitting {@code value} uses the actual current time, captured once at test start — real time, fixed snapshot.</li>
 *   <li>Omitting {@code zone} defaults to {@code UTC}.</li>
 *   <li>Omitting {@code minDate} defaults to {@code Instant.MIN} — bounds checks never fail unless explicitly tested.</li>
 *   <li>Omitting {@code maxDate} defaults to {@code Instant.MAX} — bounds checks never fail unless explicitly tested.</li>
 * </ul>
 *
 * <p>Resolution order: method-level annotation takes precedence over class-level.
 *
 * <p>Supported {@code value} formats:
 * <ul>
 *   <li>{@code yyyy-MM-dd}          — midnight in the given zone</li>
 *   <li>{@code yyyy-MM-dd HH:mm}    — date and time in the given zone</li>
 *   <li>{@code yyyy-MM-dd HH:mm:ss} — date, time and seconds in the given zone</li>
 * </ul>
 *
 * <p>Usage examples:
 * <pre>
 * // Just need the provider wired — don't care about when
 * {@literal @}TimeAware
 * class MyServiceTest { ... }
 *
 * // Fixed date, UTC
 * {@literal @}TimeAware("2024-06-15")
 * class MyServiceTest { ... }
 *
 * // Fixed date and time, specific zone
 * {@literal @}TimeAware(value = "2024-06-15 10:30", zone = "Europe/Copenhagen")
 * class MyServiceTest { ... }
 *
 * // Real current time in Copenhagen zone
 * {@literal @}TimeAware(zone = "Europe/Copenhagen")
 * class MyServiceTest { ... }
 *
 * // Method-level overrides class-level
 * {@literal @}TimeAware("2024-01-01")
 * class MyServiceTest {
 *     {@literal @}TimeAware("2024-12-31")
 *     void specificTest() { ... }
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(TimeAwareExtension.class)
public @interface TimeAware {

    /**
     * Fixed point in time. If omitted, the actual current time is used (fixed snapshot at test start).
     * <p>Accepted formats:
     * <ul>
     *   <li>{@code yyyy-MM-dd}          — midnight in the given zone</li>
     *   <li>{@code yyyy-MM-dd HH:mm}    — date and time</li>
     *   <li>{@code yyyy-MM-dd HH:mm:ss} — date, time and seconds</li>
     * </ul>
     */
    String value() default "";

    /**
     * Zone to anchor the fixed time. Defaults to UTC.
     * Example: "Europe/Copenhagen"
     */
    String zone() default "UTC";

    /**
     * Lower bound for the provider. Defaults to {@code Instant.MIN} so bounds
     * checks never accidentally fail in tests that don't care about them.
     * Override to test bound-checking behaviour explicitly.
     * Format: ISO-8601 instant string, e.g. {@code "2020-01-01T00:00:00Z"}
     */
    String minDate() default "";

    /**
     * Upper bound for the provider. Defaults to {@code Instant.MAX} so bounds
     * checks never accidentally fail in tests that don't care about them.
     * Override to test bound-checking behaviour explicitly.
     * Format: ISO-8601 instant string, e.g. {@code "2030-01-01T00:00:00Z"}
     */
    String maxDate() default "";
}
