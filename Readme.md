# Spring API Sunset

A lightweight Spring Boot 3.x starter for managing API deprecation lifecycle. Annotate endpoints with sunset dates, automatically emit standard HTTP headers (RFC 8594 / RFC 9745), track usage of deprecated endpoints via Micrometer, and return 410 Gone when an API reaches end of life.

## Features

- **Annotation-driven** — add `@Sunset` to any controller method or class
- **RFC-compliant headers** — automatically adds `Sunset` (RFC 8594), `Deprecation` (RFC 9745), and `Link` headers
- **Lifecycle enforcement** — returns `410 Gone` with Problem Detail (RFC 7807) response after the sunset date, or continues serving with warnings
- **Micrometer metrics** — request counters by endpoint and consumer, deprecated/sunset endpoint gauges
- **Actuator endpoint** — `/actuator/api-lifecycle` lists all deprecated endpoints with dates, replacements, and days remaining
- **Consumer tracking** — identify which API consumers are still hitting deprecated endpoints
- **Auto-configuration** — zero boilerplate setup with Spring Boot

## Requirements

- Java 21+
- Spring Boot 3.4+

## Quick Start

### 1. Add the dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.atlancia-labs:spring-api-sunset:0.1.0")
}
```

### 2. Annotate your endpoints

```java
@RestController
@RequestMapping("/api/v1/users")
@Sunset(date = "2025-10-01", since = "2025-04-01", replacement = "/api/v2/users")
public class UserControllerV1 {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

Per-method annotation:

```java
@RestController
public class OrderController {

    @PostMapping("/api/v1/orders")
    @Sunset(date = "2025-12-31", since = "2025-06-01",
            replacement = "/api/v2/orders", reason = "Migrating to async order processing")
    public Order createOrder(@RequestBody OrderRequest request) {
        return orderService.create(request);
    }
}
```

### 3. Response headers added automatically

Clients receive standard headers on every response from a deprecated endpoint:

```
Sunset: Wed, 01 Oct 2025 00:00:00 GMT
Deprecation: @1743465600
Link: </api/v2/users>; rel="successor-version"
```

If `since` is not specified, the `Deprecation` header is set to `true` instead of a timestamp.

### 4. After the sunset date

Once the sunset date passes, the endpoint returns `410 Gone` with a Problem Detail response:

```json
{
    "type": "about:blank",
    "title": "Gone",
    "status": 410,
    "detail": "This API endpoint was sunset on 2025-10-01. Use /api/v2/users instead.",
    "instance": "/api/v1/users/123"
}
```

## `@Sunset` Annotation

| Attribute | Required | Description |
|-----------|----------|-------------|
| `date` | Yes | Sunset date in ISO format (`YYYY-MM-DD`). After this date, the endpoint is considered retired. |
| `since` | No | Date when the endpoint was first deprecated. Used in the `Deprecation` header timestamp. |
| `replacement` | No | URL of the replacement endpoint. Added as a `Link` header with `rel="successor-version"`. |
| `reason` | No | Human-readable reason for deprecation. Shown in the actuator endpoint. |

Can be applied to individual methods or an entire controller class. Method-level annotations take precedence over class-level.

## Configuration

All properties are optional with sensible defaults:

```yaml
spring:
  api-sunset:
    enabled: true                # Enable/disable the library entirely (default: true)
    on-sunset: return-410        # RETURN_410 or CONTINUE (default: return-410)
    consumer-header: ""          # HTTP header to identify API consumers (e.g. "X-Api-Key")
```

| Property | Default | Description |
|----------|---------|-------------|
| `enabled` | `true` | Set to `false` to disable all sunset behavior |
| `on-sunset` | `RETURN_410` | `RETURN_410` blocks requests after sunset date; `CONTINUE` keeps serving with headers |
| `consumer-header` | `""` | Header name to identify consumers in metrics (empty = no consumer tracking) |

## Metrics

When Micrometer is on the classpath, the following metrics are recorded automatically:

| Metric | Type | Tags | Description |
|--------|------|------|-------------|
| `api.sunset.requests` | Counter | `endpoint`, `consumer` | Requests to deprecated/sunset endpoints |
| `api.sunset.endpoints.deprecated` | Gauge | — | Number of endpoints currently in deprecated phase |
| `api.sunset.endpoints.sunset` | Gauge | — | Number of endpoints past their sunset date |

Configure `spring.api-sunset.consumer-header` to populate the `consumer` tag with a meaningful value (e.g. API key, client ID). Otherwise defaults to `unknown`.

## Actuator

When Spring Boot Actuator is on the classpath, a lifecycle endpoint is available:

```
GET /actuator/api-lifecycle
```

```json
[
    {
        "endpoint": "GET /api/v1/users/{id}",
        "deprecated_since": "2025-04-01",
        "sunset_date": "2025-10-01",
        "replacement": "/api/v2/users",
        "days_remaining": 139,
        "phase": "DEPRECATED"
    },
    {
        "endpoint": "POST /api/v1/orders",
        "sunset_date": "2024-06-01",
        "reason": "Migrating to async order processing",
        "days_remaining": -348,
        "phase": "SUNSET"
    }
]
```

You may need to expose the endpoint in your configuration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: api-lifecycle
```

## How It Works

1. At application startup, `SunsetRegistry` scans all `@Sunset`-annotated handler methods
2. On each request, `SunsetInterceptor` checks if the handler has a sunset annotation
3. `Sunset`, `Deprecation`, and `Link` headers are added to the response
4. If the current date is past the sunset date and `on-sunset` is `RETURN_410`, the request is blocked with a `410 Gone` response
5. Metrics are recorded for every request to a deprecated endpoint

## License

Apache-2.0

---

Built by [Atlancia Labs](https://www.linkedin.com/company/atlancia-labs)
