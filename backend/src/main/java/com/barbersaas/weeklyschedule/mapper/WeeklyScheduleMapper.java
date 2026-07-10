package com.barbersaas.weeklyschedule.mapper;

import com.barbersaas.weeklyschedule.dto.WeeklyScheduleDayDto;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleResponse;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeeklyScheduleMapper {

    public WeeklyScheduleResponse toResponse(List<WeeklyScheduleEntity> entities) {
        List<WeeklyScheduleDayDto> weeklySchedule = new ArrayList<>();
        
        for (WeeklyScheduleEntity entity : entities) {
            WeeklyScheduleDayDto dto = new WeeklyScheduleDayDto(
                    entity.getDayOfWeek(),
                    entity.isWorkingDay(),
                    entity.getStartTime(),
                    entity.getEndTime()
            );
            weeklySchedule.add(dto);
        }
        
        return new WeeklyScheduleResponse(weeklySchedule);
    }

    public void updateEntity(WeeklyScheduleEntity entity, WeeklyScheduleDayDto dto) {
        entity.setWorkingDay(dto.isWorkingDay());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
    }
}