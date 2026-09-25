package com.barbersaas.bootstrap;

import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.auth.repository.AdminUserRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import com.barbersaas.barbershops.repository.BarbershopRepository;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("bootstrap")
public class BootstrapProvisioningService {
    private static final Logger log = LoggerFactory.getLogger(BootstrapProvisioningService.class);

    private final BootstrapProperties properties;
    private final BarbershopRepository barbershops;
    private final BarberRepository barbers;
    private final BarberScheduleRepository schedules;
    private final WeeklyScheduleRepository weeklySchedules;
    private final AdminUserRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public BootstrapProvisioningService(
            BootstrapProperties properties,
            BarbershopRepository barbershops,
            BarberRepository barbers,
            BarberScheduleRepository schedules,
            WeeklyScheduleRepository weeklySchedules,
            AdminUserRepository admins,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager,
            JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.barbershops = barbershops;
        this.barbers = barbers;
        this.schedules = schedules;
        this.weeklySchedules = weeklySchedules;
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void provision() {
        properties.validate();

        BarbershopEntity shop = provisionBarbershop();
        BarberEntity barber = provisionBarber(shop);
        BarberScheduleEntity schedule = provisionSchedule(barber);
        provisionWeeklySchedule(schedule);
        provisionAdmin(shop);

        log.info("Bootstrap concluído.");
    }

    private BarbershopEntity provisionBarbershop() {
        Optional<BarbershopEntity> existing = barbershops.findById(properties.getBarbershopId());
        if (existing.isPresent()) {
            BarbershopEntity shop = existing.get();
            if (!properties.getBarbershopName().equals(shop.getName())
                    || !Boolean.TRUE.equals(shop.getActive())) {
                throw new BootstrapException("O identificador da Barbershop já possui dados incompatíveis.");
            }
            log.info("Barbershop reutilizada.");
            return shop;
        }

        BarbershopEntity shop = new BarbershopEntity(
                properties.getBarbershopId(), properties.getBarbershopName(), true);
        entityManager.persist(shop);
        entityManager.flush();
        log.info("Barbershop provisionada.");
        return shop;
    }

    private BarberEntity provisionBarber(BarbershopEntity shop) {
        Optional<BarberEntity> existing = barbers.findById(properties.getBarberId());
        if (existing.isPresent()) {
            BarberEntity barber = existing.get();
            if (barber.getBarbershop() == null
                    || !shop.getId().equals(barber.getBarbershop().getId())
                    || !properties.getBarberName().equals(barber.getName())
                    || !properties.getBarberEmail().equals(barber.getEmail())
                    || !properties.getBarberPhone().equals(barber.getPhone())
                    || !Boolean.TRUE.equals(barber.getActive())) {
                throw new BootstrapException("O identificador do Barber já pertence a dados incompatíveis.");
            }
            log.info("Barber reutilizado.");
            return barber;
        }

        jdbcTemplate.update(
                """
                        INSERT INTO barbers (
                            id, name, email, phone, specialties, active, barbershop_id
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                properties.getBarberId(),
                properties.getBarberName(),
                properties.getBarberEmail(),
                properties.getBarberPhone(),
                null,
                true,
                shop.getId());
        BarberEntity barber = barbers.findById(properties.getBarberId())
                .orElseThrow(() -> new BootstrapException("Barber provisionado não pôde ser recarregado."));
        log.info("Barber provisionado.");
        return barber;
    }

    private BarberScheduleEntity provisionSchedule(BarberEntity barber) {
        Optional<BarberScheduleEntity> existing = schedules.findByBarberId(properties.getBarberId());
        if (existing.isPresent()) {
            BarberScheduleEntity schedule = existing.get();
            if (schedule.getBarber() == null
                    || !properties.getBarberId().equals(schedule.getBarber().getId())
                    || !Integer.valueOf(30).equals(schedule.getMaxBookingDays())
                    || !Integer.valueOf(5).equals(schedule.getDefaultBreakMinutes())) {
                throw new BootstrapException("A agenda do Barber possui dados incompatíveis.");
            }
            log.info("BarberSchedule reutilizada.");
            return schedule;
        }

        BarberScheduleEntity schedule = new BarberScheduleEntity(barber, 30, 5);
        entityManager.persist(schedule);
        log.info("BarberSchedule provisionada.");
        return schedule;
    }

    private void provisionWeeklySchedule(BarberScheduleEntity schedule) {
        if (schedule.getId() == null) {
            createExpectedWeeklySchedules(schedule);
            return;
        }

        List<WeeklyScheduleEntity> existing =
                weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId());
        if (existing.isEmpty()) {
            createExpectedWeeklySchedules(schedule);
            return;
        }
        if (existing.size() != 7) {
            throw new BootstrapException("A agenda semanal está incompleta e não será sobrescrita.");
        }

        Map<DayOfWeek, WeeklyScheduleEntity> byDay = new EnumMap<>(DayOfWeek.class);
        existing.forEach(day -> byDay.put(day.getDayOfWeek(), day));
        Map<DayOfWeek, ScheduleValues> expected = expectedWeeklySchedule();
        if (byDay.size() != expected.size()
                || expected.entrySet().stream().anyMatch(entry ->
                !matches(entry.getValue(), byDay.get(entry.getKey()), schedule))) {
            throw new BootstrapException("A agenda semanal possui dados incompatíveis.");
        }
        log.info("WeeklySchedules reutilizados.");
    }

    private void createExpectedWeeklySchedules(BarberScheduleEntity schedule) {
        expectedWeeklySchedule().forEach((day, values) -> {
            WeeklyScheduleEntity weekly = new WeeklyScheduleEntity(
                    schedule, day, values.start(), values.end(), values.working());
            weekly.setBreakStartTime(values.breakStart());
            weekly.setBreakEndTime(values.breakEnd());
            entityManager.persist(weekly);
        });
        log.info("WeeklySchedules provisionados.");
    }

    private boolean matches(
            ScheduleValues expected,
            WeeklyScheduleEntity actual,
            BarberScheduleEntity schedule) {
        return actual != null
                && actual.getBarberSchedule() != null
                && schedule.getId().equals(actual.getBarberSchedule().getId())
                && expected.working() == actual.isWorkingDay()
                && java.util.Objects.equals(expected.start(), actual.getStartTime())
                && java.util.Objects.equals(expected.end(), actual.getEndTime())
                && java.util.Objects.equals(expected.breakStart(), actual.getBreakStartTime())
                && java.util.Objects.equals(expected.breakEnd(), actual.getBreakEndTime());
    }

    private void provisionAdmin(BarbershopEntity shop) {
        String email = properties.getAdminEmail();
        Optional<AdminUserEntity> existing = admins.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            AdminUserEntity admin = existing.get();
            if (admin.getBarbershop() == null
                    || !shop.getId().equals(admin.getBarbershop().getId())
                    || !Boolean.TRUE.equals(admin.getActive())) {
                throw new BootstrapException("O email do Admin já pertence a outra configuração.");
            }
            log.info("Admin reutilizado.");
            return;
        }

        AdminUserEntity admin = new AdminUserEntity(
                shop,
                properties.getAdminName().trim(),
                email,
                passwordEncoder.encode(properties.getAdminPassword()),
                true);
        entityManager.persist(admin);
        log.info("Admin provisionado.");
    }

    private Map<DayOfWeek, ScheduleValues> expectedWeeklySchedule() {
        Map<DayOfWeek, ScheduleValues> values = new EnumMap<>(DayOfWeek.class);
        values.put(DayOfWeek.MONDAY, working(LocalTime.of(20, 0)));
        values.put(DayOfWeek.TUESDAY, off());
        values.put(DayOfWeek.WEDNESDAY, working(LocalTime.of(19, 30)));
        values.put(DayOfWeek.THURSDAY, working(LocalTime.of(19, 30)));
        values.put(DayOfWeek.FRIDAY, working(LocalTime.of(19, 30)));
        values.put(DayOfWeek.SATURDAY, working(LocalTime.of(19, 30)));
        values.put(DayOfWeek.SUNDAY, off());
        return values;
    }

    private ScheduleValues working(LocalTime end) {
        return new ScheduleValues(
                LocalTime.of(9, 30), end, LocalTime.NOON, LocalTime.of(14, 0), true);
    }

    private ScheduleValues off() {
        return new ScheduleValues(null, null, null, null, false);
    }

    private record ScheduleValues(
            LocalTime start,
            LocalTime end,
            LocalTime breakStart,
            LocalTime breakEnd,
            boolean working) {
    }
}
