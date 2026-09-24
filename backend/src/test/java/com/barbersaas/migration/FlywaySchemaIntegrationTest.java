package com.barbersaas.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("prod")
class FlywaySchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private UUID barbershopId;
    private UUID barberId;
    private UUID customerId;
    private UUID serviceId;
    private UUID scheduleId;

    @AfterEach
    void cleanFixture() {
        if (barbershopId == null) {
            return;
        }
        jdbc.update("DELETE FROM push_subscriptions WHERE customer_id = ?", customerId);
        jdbc.update("DELETE FROM availability_interests WHERE customer_id = ?", customerId);
        jdbc.update("DELETE FROM appointment_services WHERE service_id = ?", serviceId);
        jdbc.update("DELETE FROM appointments WHERE customer_id = ?", customerId);
        jdbc.update("DELETE FROM available_slots WHERE barber_id = ?", barberId);
        jdbc.update("DELETE FROM weekly_schedule WHERE barber_schedule_id = ?", scheduleId);
        jdbc.update("DELETE FROM schedule_blocks WHERE barber_schedule_id = ?", scheduleId);
        jdbc.update("DELETE FROM barber_schedules WHERE id = ?", scheduleId);
        jdbc.update("DELETE FROM services WHERE id = ?", serviceId);
        jdbc.update("DELETE FROM customers WHERE id = ?", customerId);
        jdbc.update("DELETE FROM barbers WHERE id = ?", barberId);
        jdbc.update("DELETE FROM barbershops WHERE id = ?", barbershopId);
        barbershopId = null;
    }

    @Test
    void flywayCreatesCurrentSchemaAndRecordsVersion() {
        assertEquals("1", jdbc.queryForObject(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank LIMIT 1",
                String.class));
        assertEquals(14, jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name <> 'flyway_schema_history'",
                Integer.class));
    }

    @Test
    void criticalIndexesAndConstraintsExist() {
        assertEquals("ux_appointments_scheduled_slot", jdbc.queryForObject(
                "SELECT to_regclass('public.ux_appointments_scheduled_slot')", String.class));
        assertEquals("ux_push_subscriptions_endpoint", jdbc.queryForObject(
                "SELECT to_regclass('public.ux_push_subscriptions_endpoint')", String.class));
        assertEquals("ix_push_subscriptions_customer_id", jdbc.queryForObject(
                "SELECT to_regclass('public.ix_push_subscriptions_customer_id')", String.class));
        assertEquals(16, jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE constraint_schema = 'public' AND constraint_type = 'FOREIGN KEY'",
                Integer.class));
        assertNotNull(jdbc.queryForObject(
                "SELECT constraint_name FROM information_schema.table_constraints "
                        + "WHERE constraint_schema = 'public' "
                        + "AND table_name = 'appointments' "
                        + "AND constraint_name = 'appointments_status_check'",
                String.class));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "FLYWAY_EMPTY_SCHEMA_TEST", matches = "true")
    void productionSchemaContainsNoDevelopmentSeedData() {
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM barbershops", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM barbers", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_users", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM push_subscriptions", Integer.class));
    }

    @Test
    void scheduledSlotIndexBlocksDoubleBookingButAllowsCanceledReuse() {
        createFixture();
        LocalDateTime slot = LocalDateTime.now().plusDays(2);

        insertAppointment(UUID.randomUUID(), slot, "cancel-a", "SCHEDULED");
        assertThrows(DataIntegrityViolationException.class,
                () -> insertAppointment(UUID.randomUUID(), slot, "cancel-b", "SCHEDULED"));
        insertAppointment(UUID.randomUUID(), slot, "cancel-c", "CANCELED");
    }

    @Test
    void criticalChecksAndUniquesRejectInvalidRows() {
        createFixture();
        LocalDateTime now = LocalDateTime.now().plusDays(3);

        assertThrows(DataIntegrityViolationException.class, () ->
                insertAppointment(UUID.randomUUID(), now, "status-check", "INVALID"));

        UUID appointmentId = UUID.randomUUID();
        insertAppointment(appointmentId, now, "duplicate-token", "SCHEDULED");
        assertThrows(DataIntegrityViolationException.class, () ->
                insertAppointment(UUID.randomUUID(), now.plusMinutes(30), "duplicate-token", "SCHEDULED"));

        jdbc.update("INSERT INTO weekly_schedule "
                        + "(id, barber_schedule_id, day_of_week, working_day) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), scheduleId, "MONDAY", true);
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbc.update("INSERT INTO weekly_schedule "
                                + "(id, barber_schedule_id, day_of_week, working_day) VALUES (?, ?, ?, ?)",
                        UUID.randomUUID(), scheduleId, "MONDAY", true));

        UUID availableSlotId = UUID.randomUUID();
        jdbc.update("INSERT INTO available_slots "
                        + "(id, barber_id, available_date_time, status, created_at) VALUES (?, ?, ?, ?, ?)",
                availableSlotId, barberId, now, "AVAILABLE", LocalDateTime.now());
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbc.update("INSERT INTO available_slots "
                                + "(id, barber_id, available_date_time, status, created_at) VALUES (?, ?, ?, ?, ?)",
                        UUID.randomUUID(), barberId, now, "AVAILABLE", LocalDateTime.now()));

        UUID endpointCustomerSubscription = UUID.randomUUID();
        jdbc.update("INSERT INTO push_subscriptions "
                        + "(id, customer_id, endpoint, p256dh, auth, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                endpointCustomerSubscription, customerId, "https://push.example.test/v1",
                "p256dh", "auth", LocalDateTime.now(), LocalDateTime.now());
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbc.update("INSERT INTO push_subscriptions "
                                + "(id, customer_id, endpoint, p256dh, auth, created_at, updated_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                        UUID.randomUUID(), customerId, "https://push.example.test/v1",
                        "p256dh-2", "auth-2", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Test
    void foreignKeysRejectUnknownReferences() {
        createFixture();
        assertThrows(DataIntegrityViolationException.class, () ->
                insertAppointment(UUID.randomUUID(), LocalDateTime.now().plusDays(4),
                        "unknown-customer", "SCHEDULED", UUID.randomUUID()));
    }

    private void createFixture() {
        barbershopId = UUID.randomUUID();
        barberId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        jdbc.update("INSERT INTO barbershops (id, name, active) VALUES (?, ?, ?)",
                barbershopId, "Schema Test", true);
        jdbc.update("INSERT INTO barbers "
                        + "(id, name, email, phone, active, barbershop_id) VALUES (?, ?, ?, ?, ?, ?)",
                barberId, "Barber Test", barberId + "@example.test", "11999999999", true, barbershopId);
        jdbc.update("INSERT INTO customers "
                        + "(id, name, phone, active, barbershop_id) VALUES (?, ?, ?, ?, ?)",
                customerId, "Customer Test", "11988888888", true, barbershopId);
        jdbc.update("INSERT INTO services "
                        + "(id, name, duration_minutes, price, active, barbershop_id) VALUES (?, ?, ?, ?, ?, ?)",
                serviceId, "Service Test", 30, 40.00, true, barbershopId);
        jdbc.update("INSERT INTO barber_schedules "
                        + "(id, barber_id, max_booking_days, default_break_minutes) VALUES (?, ?, ?, ?)",
                scheduleId, barberId, 30, 5);
    }

    private void insertAppointment(UUID id, LocalDateTime dateTime, String token, String status) {
        insertAppointment(id, dateTime, token, status, customerId);
    }

    private void insertAppointment(UUID id, LocalDateTime dateTime, String token,
                                   String status, UUID appointmentCustomerId) {
        jdbc.update("INSERT INTO appointments "
                        + "(id, customer_id, barber_id, appointment_date_time, status, created_at, cancel_token) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, appointmentCustomerId, barberId, dateTime, status, LocalDateTime.now(), token);
    }
}
