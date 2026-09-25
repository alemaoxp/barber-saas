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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BootstrapProvisioningServiceTest {

    private final BarbershopRepository barbershops = org.mockito.Mockito.mock(BarbershopRepository.class);
    private final BarberRepository barbers = org.mockito.Mockito.mock(BarberRepository.class);
    private final BarberScheduleRepository schedules = org.mockito.Mockito.mock(BarberScheduleRepository.class);
    private final WeeklyScheduleRepository weeklySchedules = org.mockito.Mockito.mock(WeeklyScheduleRepository.class);
    private final AdminUserRepository admins = org.mockito.Mockito.mock(AdminUserRepository.class);
    private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);

    @Test
    void emptyDatabaseCreatesTheCompleteBootstrapWithoutServices() {
        BootstrapProperties properties = properties();
        when(barbershops.findById(properties.getBarbershopId())).thenReturn(Optional.empty());
        BarberEntity persistedBarber = barber(properties, new BarbershopEntity(
                properties.getBarbershopId(), "Jhow Cortes", true));
        when(barbers.findById(properties.getBarberId()))
                .thenReturn(Optional.empty(), Optional.of(persistedBarber));
        when(schedules.findByBarberId(properties.getBarberId())).thenReturn(Optional.empty());
        when(admins.findByEmailIgnoreCase(properties.getAdminEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(properties.getAdminPassword())).thenReturn("bcrypt-hash");

        BootstrapProvisioningService service = service();
        service.provision();

        ArgumentCaptor<Object> persisted = ArgumentCaptor.forClass(Object.class);
        verify(entityManager, org.mockito.Mockito.atLeast(1)).persist(persisted.capture());
        List<Object> entities = persisted.getAllValues();

        BarbershopEntity shop = entities.stream()
                .filter(BarbershopEntity.class::isInstance)
                .map(BarbershopEntity.class::cast)
                .findFirst().orElseThrow();
        assertEquals(properties.getBarbershopId(), shop.getId());
        assertEquals("Jhow Cortes", shop.getName());
        assertTrue(shop.getActive());

        BarberEntity barber = persistedBarber;
        assertEquals(properties.getBarberId(), barber.getId());
        assertEquals(properties.getBarbershopId(), barber.getBarbershop().getId());
        assertEquals("Jhow", barber.getName());

        BarberScheduleEntity schedule = entities.stream()
                .filter(BarberScheduleEntity.class::isInstance)
                .map(BarberScheduleEntity.class::cast)
                .findFirst().orElseThrow();
        assertEquals(30, schedule.getMaxBookingDays());
        assertEquals(5, schedule.getDefaultBreakMinutes());

        List<WeeklyScheduleEntity> weekly = entities.stream()
                .filter(WeeklyScheduleEntity.class::isInstance)
                .map(WeeklyScheduleEntity.class::cast)
                .toList();
        assertEquals(7, weekly.size());
        WeeklyScheduleEntity monday = weekly.stream()
                .filter(day -> day.getDayOfWeek() == DayOfWeek.MONDAY).findFirst().orElseThrow();
        assertTrue(monday.isWorkingDay());
        assertEquals(LocalTime.of(9, 30), monday.getStartTime());
        assertEquals(LocalTime.of(20, 0), monday.getEndTime());
        WeeklyScheduleEntity tuesday = weekly.stream()
                .filter(day -> day.getDayOfWeek() == DayOfWeek.TUESDAY).findFirst().orElseThrow();
        assertFalse(tuesday.isWorkingDay());
        assertEquals(5, weekly.stream().filter(WeeklyScheduleEntity::isWorkingDay).count());

        AdminUserEntity admin = entities.stream()
                .filter(AdminUserEntity.class::isInstance)
                .map(AdminUserEntity.class::cast)
                .findFirst().orElseThrow();
        assertEquals(properties.getBarbershopId(), admin.getBarbershop().getId());
        assertEquals(properties.getAdminEmail(), admin.getEmail());
        assertEquals("bcrypt-hash", admin.getPasswordHash());
        assertTrue(admin.getActive());
        verify(passwordEncoder).encode(properties.getAdminPassword());
        verify(jdbcTemplate).update(
                org.mockito.ArgumentMatchers.contains("INSERT INTO barbers"),
                org.mockito.ArgumentMatchers.eq(properties.getBarberId()),
                org.mockito.ArgumentMatchers.eq("Jhow"),
                org.mockito.ArgumentMatchers.eq("jhow@jhowcortes.dev"),
                org.mockito.ArgumentMatchers.eq("(11) 99999-9999"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.eq(properties.getBarbershopId()));
    }

    @Test
    void alreadyProvisionedDatabaseIsReusedWithoutChangingPassword() {
        BootstrapProperties properties = properties();
        BarbershopEntity shop = new BarbershopEntity(properties.getBarbershopId(), "Jhow Cortes", true);
        BarberEntity barber = barber(properties, shop);
        BarberScheduleEntity schedule = new BarberScheduleEntity(barber, 30, 5);
        setId(schedule, UUID.randomUUID());
        when(barbershops.findById(properties.getBarbershopId())).thenReturn(Optional.of(shop));
        when(barbers.findById(properties.getBarberId())).thenReturn(Optional.of(barber));
        when(schedules.findByBarberId(properties.getBarberId())).thenReturn(Optional.of(schedule));
        when(weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId()))
                .thenReturn(expectedWeeklySchedules(schedule));
        AdminUserEntity admin = new AdminUserEntity(shop, properties.getAdminName(), properties.getAdminEmail(), "existing-hash", true);
        when(admins.findByEmailIgnoreCase(properties.getAdminEmail())).thenReturn(Optional.of(admin));

        service().provision();

        verify(entityManager, never()).persist(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void barberOwnedByAnotherBarbershopFailsWithoutTransfer() {
        BootstrapProperties properties = properties();
        BarbershopEntity shop = new BarbershopEntity(properties.getBarbershopId(), "Jhow Cortes", true);
        BarbershopEntity otherShop = new BarbershopEntity(UUID.randomUUID(), "Outra", true);
        BarberEntity barber = barber(properties, otherShop);
        when(barbershops.findById(properties.getBarbershopId())).thenReturn(Optional.of(shop));
        when(barbers.findById(properties.getBarberId())).thenReturn(Optional.of(barber));

        assertThrows(BootstrapException.class, () -> service().provision());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void barberWithoutBarbershopFailsWithoutNullPointerException() {
        BootstrapProperties properties = properties();
        BarbershopEntity shop = new BarbershopEntity(properties.getBarbershopId(), "Jhow Cortes", true);
        BarberEntity barber = new BarberEntity("Jhow", "jhow@jhowcortes.dev", "(11) 99999-9999", null, true);
        when(barbershops.findById(properties.getBarbershopId())).thenReturn(Optional.of(shop));
        when(barbers.findById(properties.getBarberId())).thenReturn(Optional.of(barber));

        assertThrows(BootstrapException.class, () -> service().provision());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void adminOwnedByAnotherBarbershopFailsWithoutTransfer() {
        BootstrapProperties properties = properties();
        BarbershopEntity shop = new BarbershopEntity(properties.getBarbershopId(), "Jhow Cortes", true);
        BarbershopEntity otherShop = new BarbershopEntity(UUID.randomUUID(), "Outra", true);
        BarberEntity barber = barber(properties, shop);
        BarberScheduleEntity schedule = new BarberScheduleEntity(barber, 30, 5);
        setId(schedule, UUID.randomUUID());
        when(barbershops.findById(properties.getBarbershopId())).thenReturn(Optional.of(shop));
        when(barbers.findById(properties.getBarberId())).thenReturn(Optional.of(barber));
        when(schedules.findByBarberId(properties.getBarberId())).thenReturn(Optional.of(schedule));
        when(weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId()))
                .thenReturn(expectedWeeklySchedules(schedule));
        AdminUserEntity admin = new AdminUserEntity(otherShop, "Outro", properties.getAdminEmail(), "hash", true);
        when(admins.findByEmailIgnoreCase(properties.getAdminEmail())).thenReturn(Optional.of(admin));

        assertThrows(BootstrapException.class, () -> service().provision());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void missingAdminCredentialsFailBeforeRepositoryAccess() {
        BootstrapProperties properties = properties();
        properties.setAdminPassword("");

        assertThrows(BootstrapException.class, () -> service(properties).provision());
        verifyNoInteractions(barbershops, barbers, schedules, weeklySchedules, admins, entityManager, passwordEncoder);
    }

    private BootstrapProvisioningService service() {
        return service(properties());
    }

    private BootstrapProvisioningService service(BootstrapProperties properties) {
        return new BootstrapProvisioningService(
                properties, barbershops, barbers, schedules, weeklySchedules, admins,
                passwordEncoder, entityManager, jdbcTemplate);
    }

    private BootstrapProperties properties() {
        BootstrapProperties properties = new BootstrapProperties();
        properties.setAdminName("Jhow Admin");
        properties.setAdminEmail("admin@example.test");
        properties.setAdminPassword("secret");
        return properties;
    }

    private BarberEntity barber(BootstrapProperties properties, BarbershopEntity shop) {
        BarberEntity barber = new BarberEntity("Jhow", "jhow@jhowcortes.dev", "(11) 99999-9999", null, true);
        barber.setId(properties.getBarberId());
        barber.setBarbershop(shop);
        return barber;
    }

    private List<WeeklyScheduleEntity> expectedWeeklySchedules(BarberScheduleEntity schedule) {
        List<WeeklyScheduleEntity> schedules = new java.util.ArrayList<>();
        schedules.add(working(schedule, DayOfWeek.MONDAY, LocalTime.of(20, 0)));
        schedules.add(new WeeklyScheduleEntity(schedule, DayOfWeek.TUESDAY, null, null, false));
        schedules.add(working(schedule, DayOfWeek.WEDNESDAY, LocalTime.of(19, 30)));
        schedules.add(working(schedule, DayOfWeek.THURSDAY, LocalTime.of(19, 30)));
        schedules.add(working(schedule, DayOfWeek.FRIDAY, LocalTime.of(19, 30)));
        schedules.add(working(schedule, DayOfWeek.SATURDAY, LocalTime.of(19, 30)));
        schedules.add(new WeeklyScheduleEntity(schedule, DayOfWeek.SUNDAY, null, null, false));
        return schedules;
    }

    private WeeklyScheduleEntity working(
            BarberScheduleEntity schedule, DayOfWeek day, LocalTime end) {
        WeeklyScheduleEntity weekly = new WeeklyScheduleEntity(
                schedule, day, LocalTime.of(9, 30), end, true);
        weekly.setBreakStartTime(LocalTime.NOON);
        weekly.setBreakEndTime(LocalTime.of(14, 0));
        return weekly;
    }

    private void setId(BarberScheduleEntity schedule, UUID id) {
        try {
            var field = BarberScheduleEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(schedule, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
