package com.barbersaas.weeklyschedule.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class WeeklyScheduleDayDto {

    private DayOfWeek dayOfWeek;
    private boolean workingDay; private LocalTime startTime;
    private LocalTime endTime;

    public WeeklyScheduleDayDto() {
    }

    public WeeklyScheduleDayDto(
            DayOfWeek dayOfWeek,
            boolean workingDay,
            LocalTime startTime,
            LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.workingDay = workingDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public void setWorkingDay(boolean workingDay) {
        this.workingDay = workingDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "WeeklyScheduleDayDto{" +
                "dayOfWeek=" + dayOfWeek +
                ", workingDay=" + workingDay +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}