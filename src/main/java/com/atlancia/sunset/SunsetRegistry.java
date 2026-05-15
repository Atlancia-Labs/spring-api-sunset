package com.atlancia.sunset;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SunsetRegistry implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final Map<Method, SunsetEndpointInfo> endpoints = new ConcurrentHashMap<>();
    private final List<SunsetEndpointInfo> allEndpoints = new CopyOnWriteArrayList<>();

    public SunsetRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        RequestMappingHandlerMapping handlerMapping =
                applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        handlerMapping.getHandlerMethods().forEach(this::registerIfAnnotated);
    }

    private void registerIfAnnotated(RequestMappingInfo mapping, HandlerMethod handlerMethod) {
        Sunset sunset = findSunsetAnnotation(handlerMethod);
        if (sunset == null) {
            return;
        }

        String endpoint = formatEndpoint(mapping);
        LocalDate sunsetDate = LocalDate.parse(sunset.date());
        LocalDate sinceDate = sunset.since().isEmpty() ? null : LocalDate.parse(sunset.since());
        String replacement = sunset.replacement().isEmpty() ? null : sunset.replacement();
        String reason = sunset.reason().isEmpty() ? null : sunset.reason();

        SunsetEndpointInfo info = new SunsetEndpointInfo(endpoint, sunsetDate, sinceDate, replacement, reason);
        endpoints.put(handlerMethod.getMethod(), info);
        allEndpoints.add(info);
    }

    private Sunset findSunsetAnnotation(HandlerMethod method) {
        Sunset annotation = method.getMethodAnnotation(Sunset.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(method.getBeanType(), Sunset.class);
        }
        return annotation;
    }

    private String formatEndpoint(RequestMappingInfo mapping) {
        String methods = mapping.getMethodsCondition().getMethods().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        String patterns = String.join(",", mapping.getPatternValues());
        if (methods.isEmpty()) {
            return patterns;
        }
        return methods + " " + patterns;
    }

    public SunsetEndpointInfo lookup(HandlerMethod handlerMethod) {
        return endpoints.get(handlerMethod.getMethod());
    }

    public List<SunsetEndpointInfo> getAllEndpoints() {
        return Collections.unmodifiableList(allEndpoints);
    }
}
