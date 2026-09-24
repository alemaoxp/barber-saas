package com.barbersaas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarberSaasConfigurationTest {

    private final TimeZone originalTimeZone = TimeZone.getDefault();

    @AfterEach
    void restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void developmentSeedIsRestrictedToTheLocalProfile() throws Exception {
        Class<?> seed = Class.forName(
                "com.barbersaas.barbershops.DevelopmentBarbershopSeed");
        Profile profile = seed.getAnnotation(Profile.class);

        assertTrue(profile != null);
        assertArrayEquals(new String[]{"local"}, profile.value());
    }

    @Test
    void localConfigurationRetainsTheExistingDevelopmentDefaults() throws IOException {
        String local = resource("application-local.yml");

        assertTrue(local.contains("jdbc:postgresql://localhost:5433/barberdb"));
        assertTrue(local.contains("optional:file:.env.local[.properties]"));
        assertTrue(local.contains("http://localhost:*"));
        assertTrue(local.contains("PUSH_TEST_ENABLED"));
        assertTrue(local.contains("ddl-auto: validate"));
        assertTrue(local.contains("baseline-on-migrate: false"));
        assertTrue(local.contains("org.hibernate.SQL: DEBUG"));
        assertTrue(local.contains("BasicBinder: TRACE"));
    }

    @Test
    void productionConfigurationRequiresExternalValuesAndDoesNotFallbackToLocal() throws IOException {
        String production = resource("application-prod.yml");

        assertTrue(production.contains("${DB_URL}"));
        assertTrue(production.contains("${DB_USERNAME}"));
        assertTrue(production.contains("${DB_PASSWORD}"));
        assertTrue(production.contains("${APP_CORS_ALLOWED_ORIGINS}"));
        assertTrue(production.contains("ddl-auto: validate"));
        assertFalse(production.contains("localhost"));
        assertFalse(production.contains("PUSH_TEST_VAPID_PRIVATE_KEY"));
        assertFalse(production.contains("org.hibernate.SQL: DEBUG"));
        assertFalse(production.contains("BasicBinder: TRACE"));
    }

    @Test
    void applicationTimezoneCanBePinnedToBrazilianPilotZone() {
        BarberSaasBackendApplication.configureTimeZone("America/Sao_Paulo");

        assertEquals(ZoneId.of("America/Sao_Paulo"), ZoneId.systemDefault());
    }

    private String resource(String name) throws IOException {
        ClassPathResource resource = new ClassPathResource(name);
        try (var input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
