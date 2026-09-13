package com.barbersaas.appointments.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppointmentStatusConstraintMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentStatusConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE appointments
                DROP CONSTRAINT IF EXISTS appointments_status_check
                """);
        jdbcTemplate.execute("""
                ALTER TABLE appointments
                ADD CONSTRAINT appointments_status_check
                CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELED', 'NO_SHOW'))
                """);
    }
}
