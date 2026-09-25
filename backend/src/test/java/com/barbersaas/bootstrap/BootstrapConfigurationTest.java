package com.barbersaas.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapConfigurationTest {

    @Test
    void runnerIsRestrictedToBootstrapProfileAndExplicitRunProperty() {
        Profile profile = BootstrapRunner.class.getAnnotation(Profile.class);
        ConditionalOnProperty condition = BootstrapRunner.class.getAnnotation(ConditionalOnProperty.class);

        assertTrue(profile != null);
        assertArrayEquals(new String[]{"bootstrap"}, profile.value());
        assertTrue(condition != null);
        assertArrayEquals(new String[]{"run"}, condition.name());
        assertEquals("true", condition.havingValue());
    }

    @Test
    void bootstrapProfileDisablesHttpAndReadsOnlyTemporaryAdminVariables() throws IOException {
        String config = new ClassPathResource("application-bootstrap.yml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(config.contains("web-application-type: none"));
        assertTrue(config.contains("BOOTSTRAP_ADMIN_NAME"));
        assertTrue(config.contains("BOOTSTRAP_ADMIN_EMAIL"));
        assertTrue(config.contains("BOOTSTRAP_ADMIN_PASSWORD"));
        assertTrue(!config.contains("password:" + " integration-password"));
    }
}
