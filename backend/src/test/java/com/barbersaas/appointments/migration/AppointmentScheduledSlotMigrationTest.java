package com.barbersaas.appointments.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentScheduledSlotMigrationTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ApplicationArguments arguments;

    @Test
    void duplicateScheduledSlotsStopMigrationBeforeCreatingIndex() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        AppointmentScheduledSlotMigration migration = new AppointmentScheduledSlotMigration(jdbcTemplate);

        assertThrows(IllegalStateException.class, () -> migration.run(arguments));
        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void migrationCreatesIndexWhenScheduledSlotsAreUnique() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
        AppointmentScheduledSlotMigration migration = new AppointmentScheduledSlotMigration(jdbcTemplate);

        assertDoesNotThrow(() -> migration.run(arguments));
        verify(jdbcTemplate).execute(anyString());
    }
}
