package io.github.wypeboard.foundation.date.spring.provider;

import io.github.wypeboard.foundation.date.core.provider.InstantProvider;
import io.github.wypeboard.foundation.date.spring.config.InstantProviderProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Component
public class InstantProviderImpl implements InstantProvider {

    private final Clock clock;
    private final InstantProviderProperties properties;

    public InstantProviderImpl(Clock clock, InstantProviderProperties properties) {
        this.clock = clock;
        this.properties = properties;
    }

    @Override
    public Instant now() {
        return clock.instant();
    }

    @Override
    public ZoneId zoneId() {
        return properties.getZone();
    }

    @Override
    public Instant minDate() {
        return properties.getMinDate();
    }

    @Override
    public Instant maxDate() {
        return properties.getMaxDate();
    }
}
