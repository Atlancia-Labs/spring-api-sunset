package com.atlancia.sunset;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class SunsetInterceptor implements HandlerInterceptor {

    private final SunsetRegistry registry;
    private final SunsetProperties properties;
    private final ObjectMapper objectMapper;
    private final SunsetMetrics metrics;

    public SunsetInterceptor(SunsetRegistry registry,
                             SunsetProperties properties,
                             ObjectMapper objectMapper,
                             SunsetMetrics metrics) {
        this.registry = registry;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        SunsetEndpointInfo info = registry.lookup(handlerMethod);
        if (info == null) {
            return true;
        }

        addSunsetHeaders(response, info);
        recordMetrics(request, info);

        if (info.isPastSunset() && properties.getOnSunset() == SunsetAction.RETURN_410) {
            writeGoneResponse(request, response, info);
            return false;
        }

        return true;
    }

    private void addSunsetHeaders(HttpServletResponse response, SunsetEndpointInfo info) {
        var sunsetDateTime = info.sunsetDate().atStartOfDay(ZoneOffset.UTC);
        response.setHeader("Sunset", DateTimeFormatter.RFC_1123_DATE_TIME.format(sunsetDateTime));

        if (info.sinceDate() != null) {
            long epochSeconds = info.sinceDate().atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            response.setHeader("Deprecation", "@" + epochSeconds);
        } else {
            response.setHeader("Deprecation", "true");
        }

        if (info.replacement() != null) {
            response.setHeader("Link", "<" + info.replacement() + ">; rel=\"successor-version\"");
        }
    }

    private void recordMetrics(HttpServletRequest request, SunsetEndpointInfo info) {
        if (metrics == null) {
            return;
        }
        String consumer = extractConsumer(request);
        metrics.recordRequest(info.endpoint(), consumer);
    }

    private String extractConsumer(HttpServletRequest request) {
        if (properties.getConsumerHeader().isEmpty()) {
            return "unknown";
        }
        String value = request.getHeader(properties.getConsumerHeader());
        return value != null ? value : "unknown";
    }

    private void writeGoneResponse(HttpServletRequest request, HttpServletResponse response,
                                   SunsetEndpointInfo info) throws Exception {
        response.setStatus(HttpServletResponse.SC_GONE);
        response.setContentType("application/problem+json");

        String detail = "This API endpoint was sunset on " + info.sunsetDate() + ".";
        if (info.replacement() != null) {
            detail += " Use " + info.replacement() + " instead.";
        }

        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", "about:blank");
        problem.put("title", "Gone");
        problem.put("status", 410);
        problem.put("detail", detail);
        problem.put("instance", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
