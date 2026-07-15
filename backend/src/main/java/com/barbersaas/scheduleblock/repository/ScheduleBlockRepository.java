package com.barbersaas.scheduleblock.repository;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.scheduleblock.entity.ScheduleBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlockEntity, UUID> {
    
    List<ScheduleBlockEntity> findByBarberSchedule(BarberScheduleEntity barberSchedule);
    
    Optional<ScheduleBlockEntity> findByIdAndBarberSchedule(UUID id, BarberScheduleEntity barberSchedule);
}
