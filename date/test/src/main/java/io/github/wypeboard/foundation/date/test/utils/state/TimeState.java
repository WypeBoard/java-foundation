package io.github.wypeboard.foundation.date.test.utils.state;

import io.github.wypeboard.foundation.date.core.provider.InstantProvider;
import org.mockito.MockingDetails;
import org.mockito.Mockito;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.wypeboard.foundation.date.test.utils.state.PredicateUtils.*;
import static io.github.wypeboard.foundation.date.test.utils.state.PredicateUtils.by;

public class TimeState {

    private static final ThreadLocal<TimeCache> TIME_CACHE = new ThreadLocal<>();

    public static Optional<TimeCache> getInstance() {
        return Optional.ofNullable(TIME_CACHE.get());
    }

    public static void open() {
        if (getInstance().isEmpty()) {
            TIME_CACHE.set(new TimeCache());
        }
    }

    public static boolean isOpen() {
        return getInstance().isPresent();
    }

    public static void setInstantProvider(Set<InstantProvider> instantProviders) {
        getInstance().ifPresent(timeCache -> timeCache.setInstantProviders(instantProviders));
    }

    public static void reset() {
        getInstance().ifPresent(TimeCache::reset);
    }

    public static void setZone(ZoneId zoneId) {
        getInstance().ifPresent(timeCache -> timeCache.setZoneId(zoneId));
    }

    public static void setInstantTime(Instant instant) {
        getInstance().ifPresent(timeCache -> timeCache.setInstantReference(instant));
    }

    public static void close() {
        getInstance().ifPresent(timeCache -> {
            try {
                timeCache.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        TIME_CACHE.remove();
    }


    private static class TimeCache implements Closeable {
        private Set<InstantProvider> instantProviders;
        private ZoneId zoneId = ZoneId.systemDefault();

        public void setInstantProviders(Set<InstantProvider> instantProviders) {
            this.instantProviders = instantProviders.stream()
                    .filter(throwIfNot(Objects::nonNull, "A supplied InstantProvider is null. Ensure correct order of annotation\\n@Extend mockito or Spring must be before @TimeAware"))
                    .filter(by(TimeCache::isSpyOrMock, Mockito::mockingDetails))
                    .map(TimeCache::mockNonChaningValues)
                    .collect(Collectors.toSet());
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        private static boolean isSpyOrMock(MockingDetails mockingDetails) {
            return mockingDetails.isMock() || mockingDetails.isSpy();
        }

        private static InstantProvider mockNonChaningValues(InstantProvider instantProvider) {
            Mockito.lenient().doReturn(Instant.MAX).when(instantProvider.maxDate());
            Mockito.lenient().doReturn(Instant.MIN).when(instantProvider.minDate());
            return instantProvider;
        }

        @Override
        public void close() throws IOException {
            // Present for potential MockStatic
        }

        public void reset() {
            // Present for potential MockStatic
        }

        public void setInstantReference(Instant instant) {
            StreamUtils.ofNullable(instantProviders)
                    .forEach(dateProvider -> {
                        Mockito.lenient().doReturn(instant).when(dateProvider.now());
                        Mockito.lenient().doReturn(LocalDate.ofInstant(instant, zoneId)).when(dateProvider.getDate());
                        Mockito.lenient().doReturn(LocalDateTime.ofInstant(instant, zoneId)).when(dateProvider.getDateTime());
                    });
        }
    }
}
