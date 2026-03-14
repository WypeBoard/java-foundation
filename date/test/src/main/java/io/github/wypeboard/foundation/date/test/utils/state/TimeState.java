package io.github.wypeboard.foundation.date.test.utils.state;

import io.github.wypeboard.foundation.date.core.provider.InstantProvider;
import io.github.wypeboard.foundation.utils.predicates.StreamUtils;
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

import static io.github.wypeboard.foundation.utils.predicates.PredicateUtils.by;
import static io.github.wypeboard.foundation.utils.predicates.PredicateUtils.throwIfNot;

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
        getInstance().ifPresent(cache -> cache.setInstantProviders(instantProviders));
    }

    public static void setZone(ZoneId zoneId) {
        getInstance().ifPresent(cache -> cache.setZoneId(zoneId));
    }

    public static void setInstantTime(Instant instant) {
        getInstance().ifPresent(cache -> cache.setInstantReference(instant));
    }

    public static void setMinDate(Instant minDate) {
        getInstance().ifPresent(cache -> cache.setMinDate(minDate));
    }

    public static void setMaxDate(Instant maxDate) {
        getInstance().ifPresent(cache -> cache.setMaxDate(maxDate));
    }

    public static void reset() {
        getInstance().ifPresent(TimeCache::reset);
    }

    public static void close() {
        getInstance().ifPresent(cache -> {
            try {
                cache.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        TIME_CACHE.remove();
    }

    private static class TimeCache implements Closeable {
        private Set<InstantProvider> instantProviders;
        private ZoneId zoneId = ZoneId.of("UTC");
        private Instant instant = Instant.now();
        private Instant minDate = Instant.MIN;
        private Instant maxDate = Instant.MAX;

        public void setInstantProviders(Set<InstantProvider> providers) {
            this.instantProviders = providers.stream()
                    .filter(throwIfNot(
                            Objects::nonNull,
                            "A supplied InstantProvider is null. Ensure correct extension ordering — " +
                                    "@ExtendWith MockitoExtension or SpringExtension must be declared before @TimeAware"))
                    .filter(by(details -> details.isMock() || details.isSpy(),
                            Mockito::mockingDetails))
                    .collect(Collectors.toSet());
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        public void setInstantReference(Instant instant) {
            this.instant = instant;
            stubProviders();
        }

        public void setMinDate(Instant minDate) {
            this.minDate = minDate;
        }

        public void setMaxDate(Instant maxDate) {
            this.maxDate = maxDate;
        }

        private void stubProviders() {
            StreamUtils.ofNullable(instantProviders).forEach(provider -> {
                Mockito.lenient().doReturn(instant).when(provider).now();
                Mockito.lenient().doReturn(zoneId).when(provider).zoneId();
                Mockito.lenient().doReturn(minDate).when(provider).minDate();
                Mockito.lenient().doReturn(maxDate).when(provider).maxDate();
                Mockito.lenient().doReturn(LocalDate.ofInstant(instant, zoneId)).when(provider).getDate();
                Mockito.lenient().doReturn(LocalDateTime.ofInstant(instant, zoneId)).when(provider).getDateTime();
            });
        }

        public void reset() {
            // Reserved for MockStatic teardown if added later
        }

        @Override
        public void close() throws IOException {
            // Reserved for MockStatic teardown if added later
        }
    }
}
