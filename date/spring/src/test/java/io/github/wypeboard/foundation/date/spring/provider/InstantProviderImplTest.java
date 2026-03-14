package io.github.wypeboard.foundation.date.spring.provider;

import io.github.wypeboard.foundation.date.spring.config.InstantProviderProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InstantProviderImpl")
class InstantProviderImplTest {

    private static final ZoneId    COPENHAGEN = ZoneId.of("Europe/Copenhagen");
    private static final ZoneId    UTC        = ZoneId.of("UTC");
    private static final Instant   FIXED      = Instant.parse("2024-06-15T08:30:00Z");
    private static final Instant   MIN        = Instant.parse("2020-01-01T00:00:00Z");
    private static final Instant   MAX        = Instant.parse("2030-01-01T00:00:00Z");

    @Nested
    @DisplayName("now()")
    class Now {

        @Test
        @DisplayName("returns the instant from the injected clock")
        void returnsInstantFromClock() {
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN);
            assertEquals(FIXED, provider.now());
        }

        @Test
        @DisplayName("reflects clock changes — different fixed clocks return different instants")
        void reflectsClockValue() {
            Instant other = Instant.parse("2025-01-01T00:00:00Z");
            InstantProviderImpl provider = buildProvider(other, UTC);
            assertEquals(other, provider.now());
        }
    }

    @Nested
    @DisplayName("zoneId()")
    class ZoneIdTests {

        @Test
        @DisplayName("returns zone from properties")
        void returnsZoneFromProperties() {
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN);
            assertEquals(COPENHAGEN, provider.zoneId());
        }

        @Test
        @DisplayName("returns UTC when properties configured with UTC")
        void returnsUtcWhenConfiguredAsUtc() {
            InstantProviderImpl provider = buildProvider(FIXED, UTC);
            assertEquals(UTC, provider.zoneId());
        }
    }

    @Nested
    @DisplayName("minDate() and maxDate()")
    class Bounds {

        @Test
        @DisplayName("minDate returns value from properties")
        void minDateReturnsFromProperties() {
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN, MIN, MAX);
            assertEquals(MIN, provider.minDate());
        }

        @Test
        @DisplayName("maxDate returns value from properties")
        void maxDateReturnsFromProperties() {
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN, MIN, MAX);
            assertEquals(MAX, provider.maxDate());
        }

        @Test
        @DisplayName("minDate and maxDate reflect property defaults when not overridden")
        void boundsReflectDefaults() {
            // Uses default properties — verifies defaults are sensible values
            InstantProviderProperties properties = new InstantProviderProperties();
            InstantProviderImpl provider = new InstantProviderImpl(
                    Clock.fixed(FIXED, COPENHAGEN), properties);

            // Defaults from InstantProviderProperties
            assertEquals(Instant.parse("1970-01-01T00:00:00Z"), provider.minDate());
            assertEquals(Instant.parse("2099-12-31T23:59:59Z"), provider.maxDate());
        }
    }

    @Nested
    @DisplayName("zonedNow()")
    class ZonedNow {

        @Test
        @DisplayName("returns now() in the configured zone")
        void returnsNowInConfiguredZone() {
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN);
            ZonedDateTime result = provider.zonedNow();

            assertEquals(FIXED, result.toInstant());
            assertEquals(COPENHAGEN, result.getZone());
        }

        @Test
        @DisplayName("wall clock time differs between UTC and Copenhagen for same instant")
        void wallClockDiffersBetweenZones() {
            // 08:30 UTC = 10:30 Copenhagen (UTC+2 in summer)
            ZonedDateTime inCopenhagen = buildProvider(FIXED, COPENHAGEN).zonedNow();
            ZonedDateTime inUtc        = buildProvider(FIXED, UTC).zonedNow();

            assertEquals(10, inCopenhagen.getHour());
            assertEquals(30, inCopenhagen.getMinute());
            assertEquals(8,  inUtc.getHour());
            assertEquals(30, inUtc.getMinute());

            // But they represent the same instant
            assertEquals(inUtc.toInstant(), inCopenhagen.toInstant());
        }
    }

    @Nested
    @DisplayName("getDate()")
    class GetDate {

        @Test
        @DisplayName("returns correct LocalDate in UTC")
        void returnsCorrectLocalDateInUtc() {
            InstantProviderImpl provider = buildProvider(FIXED, UTC);
            assertEquals(LocalDate.of(2024, 6, 15), provider.getDate());
        }

        @Test
        @DisplayName("returns correct LocalDate in Copenhagen — same day as UTC for morning instant")
        void returnsCorrectLocalDateInCopenhagen() {
            // 08:30 UTC = 10:30 Copenhagen — still the 15th
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN);
            assertEquals(LocalDate.of(2024, 6, 15), provider.getDate());
        }

        @Test
        @DisplayName("returns next day in Copenhagen when UTC instant is late evening")
        void returnsNextDayInCopenhagenForLateEveningUtc() {
            // 2024-06-15T23:00:00Z = 2024-06-16T01:00:00 Copenhagen (UTC+2)
            Instant lateEvening = Instant.parse("2024-06-15T23:00:00Z");
            InstantProviderImpl provider = buildProvider(lateEvening, COPENHAGEN);

            assertEquals(LocalDate.of(2024, 6, 16), provider.getDate());
        }

        @Test
        @DisplayName("returns previous day in UTC when Copenhagen instant is early morning")
        void returnsPreviousDayInUtcForEarlyMorningCopenhagen() {
            // 2024-06-15T00:30:00 Copenhagen (UTC+2) = 2024-06-14T22:30:00Z
            Instant earlyMorningCopenhagen = Instant.parse("2024-06-14T22:30:00Z");

            InstantProviderImpl copenhagenProvider = buildProvider(earlyMorningCopenhagen, COPENHAGEN);
            InstantProviderImpl utcProvider        = buildProvider(earlyMorningCopenhagen, UTC);

            assertEquals(LocalDate.of(2024, 6, 15), copenhagenProvider.getDate());
            assertEquals(LocalDate.of(2024, 6, 14), utcProvider.getDate());
        }
    }

    @Nested
    @DisplayName("getDateTime()")
    class GetDateTime {

        @Test
        @DisplayName("returns correct LocalDateTime in UTC")
        void returnsCorrectLocalDateTimeInUtc() {
            InstantProviderImpl provider = buildProvider(FIXED, UTC);
            assertEquals(LocalDateTime.of(2024, 6, 15, 8, 30), provider.getDateTime());
        }

        @Test
        @DisplayName("returns wall clock time in Copenhagen zone")
        void returnsWallClockTimeInCopenhagen() {
            // 08:30 UTC = 10:30 Copenhagen
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN);
            assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30), provider.getDateTime());
        }

        @Test
        @DisplayName("getDateTime is consistent with zonedNow toLocalDateTime")
        void getDateTimeConsistentWithZonedNow() {
            InstantProviderImpl provider = buildProvider(FIXED, COPENHAGEN);
            assertEquals(provider.zonedNow().toLocalDateTime(), provider.getDateTime());
        }
    }

    private InstantProviderImpl buildProvider(Instant instant, ZoneId zone) {
        return buildProvider(instant, zone, MIN, MAX);
    }

    private InstantProviderImpl buildProvider(Instant instant, ZoneId zone,
                                              Instant min, Instant max) {
        Clock clock = Clock.fixed(instant, zone);

        InstantProviderProperties properties = new InstantProviderProperties();
        properties.setZone(zone);
        properties.setMinDate(min);
        properties.setMaxDate(max);

        return new InstantProviderImpl(clock, properties);
    }
}