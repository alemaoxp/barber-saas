package com.barbersaas.weeklyschedule.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class WeeklyScheduleDayDto {

    private DayOfWeek dayOfWeek;
    private boolean workingDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    public WeeklyScheduleDayDto() {
    }

    public WeeklyScheduleDayDto(
            DayOfWeek dayOfWeek,
            boolean workingDay,
            LocalTime startTime,
            LocalTime endTime,
            LocalTime breakStartTime,
            LocalTime breakEndTime) {
        this.dayOfWeek = dayOfWeek;
        this.workingDay = workingDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
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

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public LocalTime getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(LocalTime breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    @Override
    public String toString() {
        return "WeeklyScheduleDayDto{" +
                "dayOfWeek=" + dayOfWeek +
                ", workingDay=" + workingDay +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", breakStartTime=" + breakStartTime +
                ", breakEndTime=" + breakEndTime +
                '}';
    }
}
