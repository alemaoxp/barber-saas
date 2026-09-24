package com.barbersaas.weeklyschedule.service;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.mapper.WeeklyScheduleMapper;
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyScheduleServiceTest {
    private static final UUID BARBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private BarberScheduleRepository barberScheduleRepository;

    private WeeklyScheduleService service;

    @BeforeEach
    void setUp() {
        service = new WeeklyScheduleService(
                weeklyScheduleRepository,
                new WeeklyScheduleMapper(),
                barberScheduleRepository
        );
    }

    @Test
    void fixedGridSlotShouldBeAccepted() {
        WeeklyScheduleEntity schedule = schedule(DayOfWeek.WEDNESDAY);
        whenSchedule(schedule);

        assertDoesNotThrow(() -> service.validateWorkingHours(
                BARBER_ID,
                java.time.LocalDateTime.of(2026, 9, 23, 10, 0)
        ));
    }

    @Test
    void timeInsideWorkingHoursButOutsideFixedGridShouldBeRejected() {
        WeeklyScheduleEntity schedule = schedule(DayOfWeek.WEDNESDAY);
        whenSchedule(schedule);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateWorkingHours(
                        BARBER_ID,
                        java.time.LocalDateTime.of(2026, 9, 23, 10, 17)
                )
        );

        assertEquals("Horário fora da grade fixa.", exception.getMessage());
    }

    private WeeklyScheduleEntity schedule(DayOfWeek dayOfWeek) {
        WeeklyScheduleEntity schedule = new WeeklyScheduleEntity(
                new BarberScheduleEntity(),
                dayOfWeek,
                LocalTime.of(9, 30),
                LocalTime.of(19, 30),
                true
        );
        schedule.setBreakStartTime(LocalTime.NOON);
        schedule.setBreakEndTime(LocalTime.of(14, 0));
        return schedule;
    }

    private void whenSchedule(WeeklyScheduleEntity schedule) {
        when(barberScheduleRepository.findByBarberId(BARBER_ID))
                .thenReturn(Optional.of(schedule.getBarberSchedule()));
        when(weeklyScheduleRepository.findByBarberScheduleIdAndDayOfWeek(
                eq(schedule.getBarberSchedule().getId()),
                eq(schedule.getDayOfWeek())
        )).thenReturn(Optional.of(schedule));
    }
}
