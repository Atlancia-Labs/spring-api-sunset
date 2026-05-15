package com.atlancia.sunset;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SunsetAutoConfiguration.class)
@ConditionalOnClass(Endpoint.class)
@ConditionalOnBean(SunsetRegistry.class)
public class SunsetActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SunsetActuatorEndpoint sunsetActuatorEndpoint(SunsetRegistry registry) {
        return new SunsetActuatorEndpoint(registry);
    }
}
