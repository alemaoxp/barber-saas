package com.barbersaas.appointments.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LegacyAppointmentServiceMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public LegacyAppointmentServiceMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Boolean legacyColumnExists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_schema = current_schema()
                      AND table_name = 'appointments'
                      AND column_name = 'service_id'
                )
                """, Boolean.class);

        if (!Boolean.TRUE.equals(legacyColumnExists)) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO appointment_services (appointment_id, service_id)
                SELECT a.id, a.service_id
                FROM appointments a
                WHERE a.service_id IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1
                      FROM appointment_services aps
                      WHERE aps.appointment_id = a.id
                        AND aps.service_id = a.service_id
                  )
                """);
        jdbcTemplate.update("""
                UPDATE appointments a
                SET total_price = s.price
                FROM services s
                WHERE a.service_id = s.id
                  AND a.total_price IS NULL
                """);
        jdbcTemplate.execute("ALTER TABLE appointments DROP COLUMN IF EXISTS service_id");
    }
}
