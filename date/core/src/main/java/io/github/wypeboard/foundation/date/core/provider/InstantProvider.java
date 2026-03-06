package io.github.wypeboard.foundation.date.core.provider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface InstantProvider {

    Instant now();
    ZoneId zoneId();

    Instant minDate();
    Instant maxDate();

    default ZonedDateTime zonedNow() {
        return now().atZone(zoneId());
    }

    /**
     * Current date in the provider's zone.
     * @return LocalDate from zoned information
     */
    default LocalDate getDate() {
        return zonedNow().toLocalDate();
    }

    /**
     *
     * @return
     */
    default LocalDateTime getDateTime() {
        return zonedNow().toLocalDateTime();
    }
}
