package io.github.wypeboard.foundation.logging.spring.config;

import io.github.wypeboard.foundation.logging.spring.tracelogging.TraceLoggingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    @Bean
    public TraceLoggingAspect traceLoggingAspect() {
        return new TraceLoggingAspect();
    }
}
