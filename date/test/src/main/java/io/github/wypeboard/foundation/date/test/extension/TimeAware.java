package io.github.wypeboard.foundation.date.test.extension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fixes the Clock for a test to a specific point in time.
 *
 * Supports:
 *   - "yyyy-MM-dd"          → midnight UTC on that date
 *   - "yyyy-MM-dd HH:mm"    → that date/time in UTC
 *
 * Resolution order: method-level annotation takes precedence over class-level.
 *
 * Any field in the test class typed {@code Clock} will be injected automatically.
 *
 * Usage:
 * <pre>
 * {@literal @}TimeAware("2024-06-15")
 * class MyServiceTest {
 *
 *     Clock clock;  // injected — fixed to 2024-06-15T00:00:00Z
 *
 *     {@literal @}TimeAware("2024-12-31 23:59")
 *     void overriddenAtMethodLevel() { ... }
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(TimeAwareExtension.class)
public @interface TimeAware {

    /**
     * Fixed point in time. Accepted formats:
     * <ul>
     *   <li>{@code yyyy-MM-dd}       — resolves to midnight UTC</li>
     *   <li>{@code yyyy-MM-dd HH:mm} — resolves to that time UTC</li>
     * </ul>
     */
    String value();

    /**
     * Zone to anchor the fixed time. Defaults to UTC.
     * Example: "Europe/Copenhagen"
     */
    String zone() default "UTC";
}
