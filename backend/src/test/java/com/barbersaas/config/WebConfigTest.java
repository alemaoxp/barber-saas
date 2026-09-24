package com.barbersaas.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebConfigTest {

    @Test
    void localOriginsAndPatchAreAllowedWithoutCredentials() {
        CorsConfiguration configuration = new WebConfig(
                "http://localhost:*,http://127.0.0.1:*")
                .corsConfiguration();

        assertEquals("http://localhost:63827",
                configuration.checkOrigin("http://localhost:63827"));
        assertEquals("http://127.0.0.1:9000",
                configuration.checkOrigin("http://127.0.0.1:9000"));
        assertTrue(configuration.getAllowedMethods().contains("PATCH"));
        assertTrue(configuration.getAllowedHeaders().contains("Authorization"));
        assertFalse(Boolean.TRUE.equals(configuration.getAllowCredentials()));
    }

    @Test
    void productionOriginListDoesNotBecomeWildcard() {
        CorsConfiguration configuration = new WebConfig(
                "https://pilot.example.com")
                .corsConfiguration();

        assertEquals("https://pilot.example.com",
                configuration.checkOrigin("https://pilot.example.com"));
        assertEquals(null, configuration.checkOrigin("http://localhost:63827"));
        assertFalse(configuration.getAllowedOriginPatterns().contains("*"));
    }
}
