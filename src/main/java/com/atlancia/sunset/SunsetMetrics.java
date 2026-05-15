package com.atlancia.sunset;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class SunsetMetrics {

    private final MeterRegistry registry;

    public SunsetMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordRequest(String endpoint, String consumer) {
        Counter.builder("api.sunset.requests")
                .tag("endpoint", endpoint)
                .tag("consumer", consumer)
                .register(registry)
                .increment();
    }

    public void registerEndpointGauges(SunsetRegistry sunsetRegistry) {
        Gauge.builder("api.sunset.endpoints.deprecated", sunsetRegistry,
                        r -> r.getAllEndpoints().stream().filter(e -> !e.isPastSunset()).count())
                .register(registry);

        Gauge.builder("api.sunset.endpoints.sunset", sunsetRegistry,
                        r -> r.getAllEndpoints().stream().filter(SunsetEndpointInfo::isPastSunset).count())
                .register(registry);
    }
}
