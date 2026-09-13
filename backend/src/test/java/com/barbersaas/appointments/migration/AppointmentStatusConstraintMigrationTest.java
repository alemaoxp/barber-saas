package com.barbersaas.appointments.migration;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppointmentStatusConstraintMigrationTest {

    @Test
    void recreatesStatusConstraintIncludingNoShow() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        new AppointmentStatusConstraintMigration(jdbcTemplate).run(null);

        verify(jdbcTemplate).execute(contains("DROP CONSTRAINT IF EXISTS appointments_status_check"));
        verify(jdbcTemplate).execute(contains("'NO_SHOW'"));
    }
}
