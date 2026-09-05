package com.barbersaas.weeklyschedule.entity;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeeklyScheduleEntityTest {

    @Test
    void shouldStoreBreakStartAndBreakEndTime() {
        BarberScheduleEntity barberSchedule = new BarberScheduleEntity();

        WeeklyScheduleEntity schedule = new WeeklyScheduleEntity(
                barberSchedule,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 30),
                LocalTime.of(20, 0),
                true
        );

        schedule.setBreakStartTime(LocalTime.of(11, 30));
        schedule.setBreakEndTime(LocalTime.of(14, 0));

        assertEquals(
                LocalTime.of(11, 30),
                schedule.getBreakStartTime()
        );

        assertEquals(
                LocalTime.of(14, 0),
                schedule.getBreakEndTime()
        );
    }
}
