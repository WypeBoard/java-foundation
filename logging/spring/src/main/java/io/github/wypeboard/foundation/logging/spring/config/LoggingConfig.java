package io.github.wypeboard.foundation.logging.spring.config;

import io.github.wypeboard.foundation.logging.spring.tracelogging.TraceLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the {@link TraceLoggingAspect} as a Spring bean.
 *
 * <h2>Enabling / disabling</h2>
 * The aspect is active by default. Set the following property to {@code false}
 * to prevent the bean from being created entirely — no AOP proxy overhead, no
 * log output:
 *
 * <pre>
 * # application.properties
 * io.github.wypeboard.foundation.tracelogging.enabled=false
 *
 * # or per-profile, e.g. application-prod.properties
 * io.github.wypeboard.foundation.tracelogging.enabled=false
 * </pre>
 *
 * {@code matchIfMissing = true} means the aspect is on when the property is
 * absent, which preserves the existing default-on behaviour.
 */
@Configuration
public class LoggingConfig {

    @Bean
    @ConditionalOnProperty(
            name = "io.github.wypeboard.foundation.tracelogging.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public TraceLoggingAspect traceLoggingAspect() {
        return new TraceLoggingAspect();
    }
}
