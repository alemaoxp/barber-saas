package com.barbersaas.barbershops;

import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import com.barbersaas.barbershops.repository.BarbershopRepository;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
class DevelopmentBarbershopSeed implements ApplicationRunner {
    static final UUID JHOW_CORTES_ID = UUID.fromString("9a5b3e91-cb71-4d77-a9d2-e1b35f4e2101");
    static final UUID DEVELOPMENT_BARBER_ID = UUID.fromString("3700633c-35f1-4ab9-af18-c60f8eb23b45");
    private final BarbershopRepository barbershops;
    private final BarberRepository barbers;
    private final BarberScheduleRepository barberSchedules;
    private final WeeklyScheduleRepository weeklySchedules;
    private final JdbcTemplate jdbcTemplate;

    DevelopmentBarbershopSeed(
            BarbershopRepository barbershops,
            BarberRepository barbers,
            BarberScheduleRepository barberSchedules,
            WeeklyScheduleRepository weeklySchedules,
            JdbcTemplate jdbcTemplate) {
        this.barbershops = barbershops;
        this.barbers = barbers;
        this.barberSchedules = barberSchedules;
        this.weeklySchedules = weeklySchedules;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        BarbershopEntity shop = barbershops.findById(JHOW_CORTES_ID)
                .orElseGet(() -> barbershops.saveAndFlush(
                        new BarbershopEntity(JHOW_CORTES_ID, "Jhow Cortes", true)
                ));
        BarberEntity barber = findOrCreateDevelopmentBarber(shop);
        BarberScheduleEntity schedule = barberSchedules.findByBarberId(DEVELOPMENT_BARBER_ID)
                .orElseGet(() -> barberSchedules.save(
                        new BarberScheduleEntity(
                                barber,
                                30,
                                5
                        )
                ));

        createMissingWeeklySchedules(schedule);
    }

    private BarberEntity findOrCreateDevelopmentBarber(BarbershopEntity shop) {
        Optional<BarberEntity> existingBarber =
                barbers.findById(DEVELOPMENT_BARBER_ID);

        if (existingBarber.isEmpty()) {
            jdbcTemplate.update(
                    """
                            INSERT INTO barbers (
                                id,
                                name,
                                email,
                                phone,
                                specialties,
                                active,
                                barbershop_id
                            ) VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    DEVELOPMENT_BARBER_ID,
                    "Jhow",
                    "jhow@jhowcortes.dev",
                    "(11) 99999-9999",
                    null,
                    true,
                    shop.getId()
            );
        }

        return existingBarber.orElseGet(() ->
                barbers.getReferenceById(DEVELOPMENT_BARBER_ID)
        );
    }

    private void createMissingWeeklySchedules(BarberScheduleEntity schedule) {
        Set<DayOfWeek> existingDays =
                EnumSet.noneOf(DayOfWeek.class);

        weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId())
                .forEach(weeklySchedule ->
                        existingDays.add(weeklySchedule.getDayOfWeek())
                );

        List<WeeklyScheduleEntity> missingSchedules =
                new ArrayList<>();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            if (!existingDays.contains(dayOfWeek)) {
                missingSchedules.add(defaultWeeklySchedule(schedule, dayOfWeek));
            }
        }

        if (!missingSchedules.isEmpty()) {
            weeklySchedules.saveAll(missingSchedules);
        }
    }

    private WeeklyScheduleEntity defaultWeeklySchedule(
            BarberScheduleEntity schedule,
            DayOfWeek dayOfWeek) {

        if (dayOfWeek == DayOfWeek.TUESDAY
                || dayOfWeek == DayOfWeek.SUNDAY) {
            return new WeeklyScheduleEntity(
                    schedule,
                    dayOfWeek,
                    null,
                    null,
                    false
            );
        }

        LocalTime endTime =
                dayOfWeek == DayOfWeek.MONDAY
                        ? LocalTime.of(20, 0)
                        : LocalTime.of(19, 30);

        WeeklyScheduleEntity weeklySchedule =
                new WeeklyScheduleEntity(
                        schedule,
                        dayOfWeek,
                        LocalTime.of(9, 30),
                        endTime,
                        true
                );
        weeklySchedule.setBreakStartTime(LocalTime.NOON);
        weeklySchedule.setBreakEndTime(LocalTime.of(14, 0));
        return weeklySchedule;
    }
}
