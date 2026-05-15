package com.atlancia.sunset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SunsetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deprecatedEndpoint_addsSunsetHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/test"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Sunset"))
                .andExpect(header().exists("Deprecation"))
                .andExpect(header().string("Link", containsString("/api/v2/test")));
    }

    @Test
    void deprecatedEndpoint_setsDeprecationTimestamp() throws Exception {
        mockMvc.perform(get("/api/v1/test"))
                .andExpect(header().string("Deprecation", "@1735689600"));
    }

    @Test
    void sunsetEndpoint_returns410() throws Exception {
        mockMvc.perform(get("/api/v1/expired"))
                .andExpect(status().isGone())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(410))
                .andExpect(jsonPath("$.title").value("Gone"));
    }

    @Test
    void sunsetEndpoint_includesReplacementInDetail() throws Exception {
        mockMvc.perform(get("/api/v1/expired-with-replacement"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.detail", containsString("/api/v2/replaced")));
    }

    @Test
    void nonAnnotatedEndpoint_noSunsetHeaders() throws Exception {
        mockMvc.perform(get("/api/v2/test"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Sunset"))
                .andExpect(header().doesNotExist("Deprecation"));
    }

    @Test
    void deprecatedEndpointWithoutSince_setsDeprecationTrue() throws Exception {
        mockMvc.perform(get("/api/v1/no-since"))
                .andExpect(header().string("Deprecation", "true"));
    }

    @SpringBootApplication
    static class TestApp {

        @RestController
        static class TestController {

            @GetMapping("/api/v1/test")
            @Sunset(date = "2099-12-31", since = "2025-01-01", replacement = "/api/v2/test")
            public String deprecated() {
                return "ok";
            }

            @GetMapping("/api/v1/expired")
            @Sunset(date = "2020-01-01", since = "2019-01-01")
            public String expired() {
                return "should not reach here";
            }

            @GetMapping("/api/v1/expired-with-replacement")
            @Sunset(date = "2020-01-01", replacement = "/api/v2/replaced")
            public String expiredWithReplacement() {
                return "should not reach here";
            }

            @GetMapping("/api/v1/no-since")
            @Sunset(date = "2099-12-31")
            public String noSince() {
                return "ok";
            }

            @GetMapping("/api/v2/test")
            public String current() {
                return "ok";
            }
        }
    }
}
