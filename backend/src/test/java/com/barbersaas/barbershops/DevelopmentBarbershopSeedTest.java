package com.barbersaas.barbershops;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DevelopmentBarbershopSeedTest {

    @Test
    void runShouldInsertDevelopmentBarberOnEmptyDatabase() {
        BarbershopRepository barbershops = mock(BarbershopRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        BarberScheduleRepository barberSchedules =
                mock(BarberScheduleRepository.class);
        WeeklyScheduleRepository weeklySchedules =
                mock(WeeklyScheduleRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BarbershopEntity shop =
                new BarbershopEntity(
                        DevelopmentBarbershopSeed.JHOW_CORTES_ID,
                        "Jhow Cortes",
                        true
                );
        BarberEntity barber = new BarberEntity();
        BarberScheduleEntity schedule =
                new BarberScheduleEntity(
                        barber,
                        30,
                        5
                );
        ReflectionTestUtils.setField(
                schedule,
                "id",
                UUID.fromString("00000000-0000-0000-0000-000000000010")
        );

        when(barbershops.findById(DevelopmentBarbershopSeed.JHOW_CORTES_ID))
                .thenReturn(Optional.empty());
        when(barbershops.saveAndFlush(any(BarbershopEntity.class)))
                .thenReturn(shop);
        when(barbers.findById(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(Optional.empty());
        when(barbers.getReferenceById(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(barber);
        when(barberSchedules.findByBarberId(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(Optional.empty());
        when(barberSchedules.save(any(BarberScheduleEntity.class)))
                .thenReturn(schedule);
        when(weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId()))
                .thenReturn(List.of());

        new DevelopmentBarbershopSeed(
                barbershops,
                barbers,
                barberSchedules,
                weeklySchedules,
                jdbcTemplate
        ).run(null);

        verify(jdbcTemplate).update(
                contains("INSERT INTO barbers"),
                eq(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID),
                eq("Jhow"),
                eq("jhow@jhowcortes.dev"),
                eq("(11) 99999-9999"),
                isNull(),
                eq(true),
                eq(DevelopmentBarbershopSeed.JHOW_CORTES_ID)
        );
        verify(weeklySchedules).saveAll(anyList());
    }

    @Test
    void runShouldCreateWeeklyScheduleRequiredByDailyAgendaOnEmptyDatabase() {
        BarbershopRepository barbershops = mock(BarbershopRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        BarberScheduleRepository barberSchedules =
                mock(BarberScheduleRepository.class);
        WeeklyScheduleRepository weeklySchedules =
                mock(WeeklyScheduleRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BarbershopEntity shop =
                new BarbershopEntity(
                        DevelopmentBarbershopSeed.JHOW_CORTES_ID,
                        "Jhow Cortes",
                        true
                );
        BarberEntity barber = new BarberEntity();
        BarberScheduleEntity schedule =
                new BarberScheduleEntity(
                        barber,
                        30,
                        5
                );
        ReflectionTestUtils.setField(
                schedule,
                "id",
                UUID.fromString("00000000-0000-0000-0000-000000000011")
        );

        when(barbershops.findById(DevelopmentBarbershopSeed.JHOW_CORTES_ID))
                .thenReturn(Optional.of(shop));
        when(barbers.findById(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(Optional.empty());
        when(barbers.getReferenceById(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(barber);
        when(barberSchedules.findByBarberId(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(Optional.empty());
        when(barberSchedules.save(any(BarberScheduleEntity.class)))
                .thenReturn(schedule);
        when(weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId()))
                .thenReturn(List.of());

        new DevelopmentBarbershopSeed(
                barbershops,
                barbers,
                barberSchedules,
                weeklySchedules,
                jdbcTemplate
        ).run(null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<WeeklyScheduleEntity>> schedulesCaptor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(weeklySchedules).saveAll(schedulesCaptor.capture());

        List<WeeklyScheduleEntity> savedSchedules =
                StreamSupport.stream(
                                schedulesCaptor.getValue().spliterator(),
                                false
                        )
                        .toList();

        assertEquals(7, savedSchedules.size());

        WeeklyScheduleEntity monday =
                findByDay(savedSchedules, DayOfWeek.MONDAY);
        assertTrue(monday.isWorkingDay());
        assertEquals(LocalTime.of(9, 30), monday.getStartTime());
        assertEquals(LocalTime.of(20, 0), monday.getEndTime());
        assertEquals(LocalTime.NOON, monday.getBreakStartTime());
        assertEquals(LocalTime.of(14, 0), monday.getBreakEndTime());

        WeeklyScheduleEntity tuesday =
                findByDay(savedSchedules, DayOfWeek.TUESDAY);
        assertFalse(tuesday.isWorkingDay());
        assertEquals(null, tuesday.getStartTime());
        assertEquals(null, tuesday.getEndTime());

        for (DayOfWeek dayOfWeek : List.of(
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        )) {
            WeeklyScheduleEntity scheduleDay =
                    findByDay(savedSchedules, dayOfWeek);
            assertTrue(scheduleDay.isWorkingDay());
            assertEquals(LocalTime.of(9, 30), scheduleDay.getStartTime());
            assertEquals(LocalTime.of(19, 30), scheduleDay.getEndTime());
            assertEquals(LocalTime.NOON, scheduleDay.getBreakStartTime());
            assertEquals(LocalTime.of(14, 0), scheduleDay.getBreakEndTime());
        }

        WeeklyScheduleEntity sunday =
                findByDay(savedSchedules, DayOfWeek.SUNDAY);
        assertFalse(sunday.isWorkingDay());
        assertEquals(null, sunday.getStartTime());
        assertEquals(null, sunday.getEndTime());
    }

    @Test
    void runShouldNotCreateDuplicatesWhenDevelopmentScheduleAlreadyExists() {
        BarbershopRepository barbershops = mock(BarbershopRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        BarberScheduleRepository barberSchedules =
                mock(BarberScheduleRepository.class);
        WeeklyScheduleRepository weeklySchedules =
                mock(WeeklyScheduleRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BarbershopEntity shop =
                new BarbershopEntity(
                        DevelopmentBarbershopSeed.JHOW_CORTES_ID,
                        "Jhow Cortes",
                        true
                );
        BarberEntity barber = new BarberEntity();
        BarberScheduleEntity schedule =
                new BarberScheduleEntity(
                        barber,
                        30,
                        5
                );
        ReflectionTestUtils.setField(
                schedule,
                "id",
                UUID.fromString("00000000-0000-0000-0000-000000000012")
        );

        when(barbershops.findById(DevelopmentBarbershopSeed.JHOW_CORTES_ID))
                .thenReturn(Optional.of(shop));
        when(barbers.findById(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(Optional.of(new BarberEntity()));
        when(barberSchedules.findByBarberId(DevelopmentBarbershopSeed.DEVELOPMENT_BARBER_ID))
                .thenReturn(Optional.of(schedule));
        when(weeklySchedules.findByBarberScheduleIdOrderByDayOfWeek(schedule.getId()))
                .thenReturn(List.of(
                        weeklySchedule(schedule, DayOfWeek.MONDAY),
                        weeklySchedule(schedule, DayOfWeek.TUESDAY),
                        weeklySchedule(schedule, DayOfWeek.WEDNESDAY),
                        weeklySchedule(schedule, DayOfWeek.THURSDAY),
                        weeklySchedule(schedule, DayOfWeek.FRIDAY),
                        weeklySchedule(schedule, DayOfWeek.SATURDAY),
                        weeklySchedule(schedule, DayOfWeek.SUNDAY)
                ));

        new DevelopmentBarbershopSeed(
                barbershops,
                barbers,
                barberSchedules,
                weeklySchedules,
                jdbcTemplate
        ).run(null);

        verifyNoInteractions(jdbcTemplate);
        verify(weeklySchedules, never()).saveAll(anyList());
    }

    private static WeeklyScheduleEntity weeklySchedule(
            BarberScheduleEntity schedule,
            DayOfWeek dayOfWeek) {

        return new WeeklyScheduleEntity(
                schedule,
                dayOfWeek,
                null,
                null,
                false
        );
    }

    private static WeeklyScheduleEntity findByDay(
            List<WeeklyScheduleEntity> schedules,
            DayOfWeek dayOfWeek) {

        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow();
    }
}
