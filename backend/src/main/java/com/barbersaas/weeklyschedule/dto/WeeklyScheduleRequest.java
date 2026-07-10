package com.barbersaas.weeklyschedule.dto;

import java.util.List;

public class WeeklyScheduleRequest {

    private List<WeeklyScheduleDayDto> weeklySchedule;

    public WeeklyScheduleRequest() {
    }

    public WeeklyScheduleRequest(List<WeeklyScheduleDayDto> weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
    }
    public List<WeeklyScheduleDayDto> getWeeklySchedule() {
        return weeklySchedule;
    }

    public void setWeeklySchedule(List<WeeklyScheduleDayDto> weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
    }

    @Override
    public String toString() {
        return "WeeklyScheduleRequest{" +
                "weeklySchedule=" + weeklySchedule +
                '}';
    }
}