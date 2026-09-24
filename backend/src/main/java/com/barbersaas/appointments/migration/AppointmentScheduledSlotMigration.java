package com.barbersaas.appointments.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppointmentScheduledSlotMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentScheduledSlotMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer duplicateGroups = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT barber_id, appointment_date_time
                    FROM appointments
                    WHERE status = 'SCHEDULED'
                    GROUP BY barber_id, appointment_date_time
                    HAVING COUNT(*) > 1
                ) duplicates
                """, Integer.class);

        if (duplicateGroups != null && duplicateGroups > 0) {
            throw new IllegalStateException(
                    "Não foi possível proteger os horários: existem agendamentos SCHEDULED duplicados."
            );
        }

        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_appointments_scheduled_slot
                ON appointments (barber_id, appointment_date_time)
                WHERE status = 'SCHEDULED'
                """);
    }
}
