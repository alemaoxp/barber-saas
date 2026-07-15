package com.barbersaas.scheduleblock.mapper;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.scheduleblock.dto.CreateScheduleBlockRequest;
import com.barbersaas.scheduleblock.dto.ScheduleBlockResponse;
import com.barbersaas.scheduleblock.dto.UpdateScheduleBlockRequest;
import com.barbersaas.scheduleblock.entity.ScheduleBlockEntity;
import org.springframework.stereotype.Component;

@Component
public class ScheduleBlockMapper {

    public ScheduleBlockEntity toEntity(CreateScheduleBlockRequest request, BarberScheduleEntity barberSchedule) {
        return new ScheduleBlockEntity(
                barberSchedule,
                request.getStartDateTime(),
                request.getEndDateTime(),
                request.getReason()
        );
    }

    public void updateEntity(ScheduleBlockEntity entity, UpdateScheduleBlockRequest request) {
        entity.setStartDateTime(request.getStartDateTime());
        entity.setEndDateTime(request.getEndDateTime());
        entity.setReason(request.getReason());
    }

    public ScheduleBlockResponse toResponse(ScheduleBlockEntity entity) {
        return new ScheduleBlockResponse(
                entity.getId(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}