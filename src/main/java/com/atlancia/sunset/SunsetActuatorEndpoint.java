package com.atlancia.sunset;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Endpoint(id = "api-lifecycle")
public class SunsetActuatorEndpoint {

    private final SunsetRegistry registry;

    public SunsetActuatorEndpoint(SunsetRegistry registry) {
        this.registry = registry;
    }

    @ReadOperation
    public List<Map<String, Object>> lifecycle() {
        return registry.getAllEndpoints().stream()
                .map(this::toMap)
                .toList();
    }

    private Map<String, Object> toMap(SunsetEndpointInfo info) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("endpoint", info.endpoint());
        if (info.sinceDate() != null) {
            entry.put("deprecated_since", info.sinceDate().toString());
        }
        entry.put("sunset_date", info.sunsetDate().toString());
        if (info.replacement() != null) {
            entry.put("replacement", info.replacement());
        }
        if (info.reason() != null) {
            entry.put("reason", info.reason());
        }
        entry.put("days_remaining", info.daysUntilSunset());
        entry.put("phase", info.phase());
        return entry;
    }
}
