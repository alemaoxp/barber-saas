package com.barbersaas.appointments.migration;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegacyAppointmentServiceMigrationTest {

    @Test
    void migratesLegacyServiceIntoJoinTableBeforeDroppingColumn() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Boolean.class)))
                .thenReturn(true);

        new LegacyAppointmentServiceMigration(jdbcTemplate).run(null);

        verify(jdbcTemplate).update(org.mockito.ArgumentMatchers.contains("appointment_services"));
        verify(jdbcTemplate).update(org.mockito.ArgumentMatchers.contains("total_price"));
        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains("DROP COLUMN IF EXISTS service_id"));
    }

    @Test
    void skipsMigrationWhenLegacyColumnIsAlreadyGone() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Boolean.class)))
                .thenReturn(false);

        new LegacyAppointmentServiceMigration(jdbcTemplate).run(null);

        verify(jdbcTemplate, never()).update(anyString());
        verify(jdbcTemplate, never()).execute(anyString());
    }
}
