package com.atlancia.sunset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(after = SunsetMetricsAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "spring.api-sunset", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(SunsetProperties.class)
public class SunsetAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SunsetRegistry sunsetRegistry(ApplicationContext applicationContext,
                                         @Nullable SunsetMetrics metrics) {
        SunsetRegistry registry = new SunsetRegistry(applicationContext);
        if (metrics != null) {
            metrics.registerEndpointGauges(registry);
        }
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public SunsetInterceptor sunsetInterceptor(SunsetRegistry registry,
                                               SunsetProperties properties,
                                               ObjectMapper objectMapper,
                                               @Nullable SunsetMetrics metrics) {
        return new SunsetInterceptor(registry, properties, objectMapper, metrics);
    }

    @Bean
    public WebMvcConfigurer sunsetWebMvcConfigurer(SunsetInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}
