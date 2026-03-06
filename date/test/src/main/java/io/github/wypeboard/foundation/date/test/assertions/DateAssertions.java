package io.github.wypeboard.foundation.date.test.assertions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DateAssertions {

    private DateAssertions() {
        // Private
    }

    public static void assertInstantEquals(Instant expected, Instant actual) {
        assertEquals(expected, actual,
                () -> "Expected instant %s but was %s".formatted(expected, actual));
    }

    public static void assertBefore(Instant reference, Instant actual) {
        assertTrue(actual.isBefore(reference),
                () -> "%s should be before %s".formatted(actual, reference));
    }

    public static void assertAfter(Instant reference, Instant actual) {
        assertTrue(actual.isAfter(reference),
                () -> "%s should be after %s".formatted(actual, reference));
    }

    public static void assertSameDayInZone(Instant a, Instant b, ZoneId zone) {
        LocalDate dayA = a.atZone(zone).toLocalDate();
        LocalDate dayB = b.atZone(zone).toLocalDate();
        assertEquals(dayA, dayB,
                () -> "Expected same day in zone %s but got %s vs %s"
                        .formatted(zone, dayA, dayB));
    }

    public static void assertWithinSeconds(Instant expected, Instant actual, long toleranceSeconds) {
        long diff = Math.abs(ChronoUnit.SECONDS.between(expected, actual));
        assertTrue(diff <= toleranceSeconds,
                () -> "Expected %s to be within %ds of %s but diff was %ds"
                        .formatted(actual, toleranceSeconds, expected, diff));
    }

    public static void assertSameZone(ZoneId expected, ZonedDateTime actual) {
        assertEquals(expected, actual.getZone(),
                () -> "Expected zone %s but was %s".formatted(expected, actual.getZone()));
    }
}
