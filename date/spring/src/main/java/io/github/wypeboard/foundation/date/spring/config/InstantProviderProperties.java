package io.github.wypeboard.foundation.date.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.time.ZoneId;

@ConfigurationProperties(prefix = "foundation.date")
public class InstantProviderProperties {

    private ZoneId zone = ZoneId.of("Europe/Copenhagen");
    private Instant minDate = Instant.parse("1970-01-01T00:00:00Z");
    private Instant maxDate = Instant.parse("2099-12-31T23:59:59Z");

    public ZoneId getZone() {
        return zone;
    }

    public void setZone(ZoneId zone) {
        this.zone = zone;
    }

    public Instant getMinDate() {
        return minDate;
    }

    public void setMinDate(Instant minDate) {
        this.minDate = minDate;
    }

    public Instant getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Instant maxDate) {
        this.maxDate = maxDate;
    }
}
