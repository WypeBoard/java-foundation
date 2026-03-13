package io.github.wypeboard.foundation.date.test.extension;

import io.github.wypeboard.foundation.date.core.provider.InstantProvider;
import io.github.wypeboard.foundation.date.test.utils.state.TimeState;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
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
 * <p>
 * Lifecycle:
 * 1. beforeEach  — resolves annotation (method > class), builds fixed Clock, injects into Clock fields
 * 2. afterEach   — clears injected fields back to null (clean state between tests)
 */
public class TimeAwareExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        TimeState.open();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        TimeState.close();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ExtentionHelper.retrieveAnnotationFromTestClasses(TimeAware.class, context).ifPresent(settings -> {
            TimeState.setInstantProvider(ExtentionHelper.getFieldsFromInstanceHierarchy(context, InstantProvider.class));

            ZoneId zoneId = ZoneId.of(settings.zone());
            TimeState.setZone(zoneId);
            TimeState.setInstantTime(parse(settings.value(), zoneId));
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TimeState.reset();
    }

    private Instant parse(String value, ZoneId zone) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                return localDateTime.atZone(zone).toInstant();
            } catch (DateTimeParseException e) {
                // Ignore. Try next formatter
            }
        }

        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(zone).toInstant();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Unable to parse @TimeAware value");
        }
    }
}