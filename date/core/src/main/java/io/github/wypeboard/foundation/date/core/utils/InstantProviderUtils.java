package io.github.wypeboard.foundation.date.core.utils;

import io.github.wypeboard.foundation.date.core.domain.Interval;
import io.github.wypeboard.foundation.date.core.provider.InstantProvider;

import java.time.Instant;

/**
 * Static utilities that operate on an InstantProvider.
 * Bounds checking, range creation, and validation live here — not on the interface.
 */
public class InstantProviderUtils {

    private InstantProviderUtils() {
        // Utils class
    }

    public static boolean isWithinBounds(InstantProvider provider, Instant instant) {
        return !instant.isBefore(provider.minDate())
                && !instant.isAfter(provider.maxDate());
    }

    public static void requireWithinBounds(InstantProvider provider, Instant instant) {
        if (!isWithinBounds(provider, instant)) {
            throw new DateOutOfBoundsException(instant, provider.minDate(), provider.maxDate());
        }
    }

    /**
     * Build an Interval spanning from now() to maxDate().
     */
    public static Interval remainingRange(InstantProvider provider) {
        return new Interval(provider.now(), provider.maxDate());
    }

    /**
     * Build an Interval spanning from minDate() to now().
     */
    public static Interval elapsedRange(InstantProvider provider) {
        return new Interval(provider.minDate(), provider.now());
    }

    /**
     * Build the full valid range from minDate() to maxDate().
     */
    public static Interval fullRange(InstantProvider provider) {
        return new Interval(provider.minDate(), provider.maxDate());
    }


    public static class DateOutOfBoundsException extends RuntimeException {
        public DateOutOfBoundsException(Instant instant, Instant min, Instant max) {
            super("Instant [%s] is outside allowed bounds [%s, %s]"
                    .formatted(instant, min, max));
        }
    }
}
