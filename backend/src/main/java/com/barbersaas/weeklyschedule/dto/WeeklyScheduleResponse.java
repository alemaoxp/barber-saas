package com.barbersaas.weeklyschedule.dto;

import java.util.List;

public class WeeklyScheduleResponse {

    private List<WeeklyScheduleDayDto> weeklySchedule;

    public WeeklyScheduleResponse() {
    }

    public WeeklyScheduleResponse(List<WeeklyScheduleDayDto> weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
    }

    public List<WeeklyScheduleDayDto> getWeeklySchedule() {
        return weeklySchedule;
    }

    public void setWeeklySchedule(List<WeeklyScheduleDayDto> weeklySchedule) {this.weeklySchedule = weeklySchedule;
    }

    @Override
    public String toString() {
        return "WeeklyScheduleResponse{" +
                "weeklySchedule=" + weeklySchedule +
                '}';
    }
}