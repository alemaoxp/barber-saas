package com.barbersaas.bootstrap;

import com.barbersaas.auth.AdminAuthService;
import com.barbersaas.auth.dto.AdminLoginResponse;
import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.auth.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "bootstrap.run=false",
        "spring.main.web-application-type=none",
        "bootstrap.admin-name=Integration Admin",
        "bootstrap.admin-email=bootstrap-integration@example.test",
        "bootstrap.admin-password=integration-password"
})
@ActiveProfiles({"prod", "bootstrap"})
@EnabledIf(expression = "#{systemEnvironment['BOOTSTRAP_POSTGRES_TEST'] == 'true'}",
        loadContext = false)
class BootstrapPostgresIntegrationTest {

    @Autowired
    private BootstrapProvisioningService provisioningService;

    @Autowired
    private BootstrapProperties properties;

    @Autowired
    private AdminUserRepository admins;

    @Autowired
    private AdminAuthService authService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void emptyFlywaySchemaCanBeProvisionedAndLoggedIntoTwice() {
        assertEquals(0, count("barbershops"));
        assertEquals(0, count("barbers"));
        assertEquals(0, count("admin_users"));

        provisioningService.provision();

        assertEquals(1, count("barbershops"));
        assertEquals(1, count("barbers"));
        assertEquals(1, count("barber_schedules"));
        assertEquals(7, count("weekly_schedule"));
        assertEquals(1, count("admin_users"));
        assertEquals(0, count("services"));
        assertEquals(0, count("customers"));
        assertEquals(0, count("appointments"));

        AdminUserEntity admin = admins.findByEmailIgnoreCase(properties.getAdminEmail()).orElseThrow();
        assertEquals(properties.getBarbershopId(), admin.getBarbershop().getId());
        assertTrue(admin.getPasswordHash().startsWith("$2"));
        assertTrue(!admin.getPasswordHash().contains("integration-password"));
        AdminLoginResponse login = authService.login(properties.getAdminEmail(), properties.getAdminPassword());
        assertNotNull(login.token());
        assertEquals(properties.getBarbershopId(), login.user().barbershopId());

        String hash = admin.getPasswordHash();
        provisioningService.provision();

        assertEquals(1, count("barbershops"));
        assertEquals(1, count("barbers"));
        assertEquals(1, count("barber_schedules"));
        assertEquals(7, count("weekly_schedule"));
        assertEquals(1, count("admin_users"));
        assertEquals(hash, admins.findByEmailIgnoreCase(properties.getAdminEmail())
                .orElseThrow().getPasswordHash());
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
