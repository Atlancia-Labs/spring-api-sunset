package com.atlancia.sunset;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class SunsetMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SunsetMetrics sunsetMetrics(MeterRegistry registry) {
        return new SunsetMetrics(registry);
    }
}
