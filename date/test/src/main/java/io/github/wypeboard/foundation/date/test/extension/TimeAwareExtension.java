package io.github.wypeboard.foundation.date.test.extension;

import io.github.wypeboard.foundation.date.core.provider.InstantProvider;
import io.github.wypeboard.foundation.date.test.utils.state.TimeState;
import io.github.wypeboard.foundation.utils.test.extension.ExtentionHelper;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * JUnit 5 extension backing {@link TimeAware}.
 *
 * <p>Lifecycle per test:
 * <ol>
 *   <li>beforeEach — opens TimeState, resolves annotation (method > class),
 *       builds a fixed Clock, stubs any {@link InstantProvider} mocks found
 *       in the test instance hierarchy.</li>
 *   <li>afterEach  — resets and closes TimeState.</li>
 * </ol>
 */
public class TimeAwareExtension implements BeforeEachCallback, AfterEachCallback {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );


    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        TimeState.open();

        ExtentionHelper.retrieveAnnotationFromTestClasses(TimeAware.class, context)
                .ifPresent(settings -> apply(settings, context));

        ExtentionHelper.retrieveAnnotationFromTestClasses(TimeAware.class, context).ifPresent(settings -> {
            TimeState.setInstantProvider(ExtentionHelper.getFieldsFromInstanceHierarchy(context, InstantProvider.class));

            ZoneId zoneId = ZoneId.of(settings.zone());
            TimeState.setZone(zoneId);
            TimeState.setInstantTime(parse(settings.value(), zoneId));
        });
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TimeState.reset();
        TimeState.close();
    }

    private void apply(TimeAware settings, ExtensionContext context) {
        ZoneId zone    = ZoneId.of(settings.zone());
        Instant instant = resolveInstant(settings.value(), zone);
        Instant minDate = resolveMin(settings.minDate());
        Instant maxDate = resolveMax(settings.maxDate());

        TimeState.setZone(zone);
        TimeState.setInstantTime(instant);
        TimeState.setMinDate(minDate);
        TimeState.setMaxDate(maxDate);
        TimeState.setInstantProvider(
                ExtentionHelper.getFieldsFromInstanceHierarchy(context, InstantProvider.class));
    }

    /**
     * Resolves the instant from the annotation value.
     * Empty value → current time captured as a fixed snapshot.
     */
    private Instant resolveInstant(String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return Instant.now(java.time.Clock.system(zone));
        }
        return parse(value, zone);
    }

    private Instant resolveMin(String value) {
        if (value == null || value.isBlank()) {
            return Instant.MIN;
        }
        return Instant.parse(value);
    }

    private Instant resolveMax(String value) {
        if (value == null || value.isBlank()) {
            return Instant.MAX;
        }
        return Instant.parse(value);
    }

    private Instant parse(String value, ZoneId zone) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                // Date-only formatter produces LocalDate, others produce LocalDateTime
                if (formatter.toString().contains("HH")) {
                    return LocalDateTime.parse(value, formatter).atZone(zone).toInstant();
                } else {
                    return LocalDate.parse(value, formatter).atStartOfDay(zone).toInstant();
                }
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        throw new RuntimeException(
                "@TimeAware could not parse value '%s' — accepted formats: yyyy-MM-dd, yyyy-MM-dd HH:mm, yyyy-MM-dd HH:mm:ss"
                        .formatted(value));
    }
}