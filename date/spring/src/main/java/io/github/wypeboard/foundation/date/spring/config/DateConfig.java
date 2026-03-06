package io.github.wypeboard.foundation.date.spring.config;

import io.github.wypeboard.foundation.date.core.provider.InstantProvider;
import io.github.wypeboard.foundation.date.spring.provider.InstantProviderImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties({InstantProviderProperties.class})
public class DateConfig {

    @Bean
    public Clock clock(InstantProviderProperties properties) {
        return Clock.system(properties.getZone());
    }


    @Bean
    public InstantProvider instantProvider(Clock clock,
                                           InstantProviderProperties properties) {
        return new InstantProviderImpl(clock, properties);
    }
}
