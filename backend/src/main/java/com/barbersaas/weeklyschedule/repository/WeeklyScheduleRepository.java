package com.barbersaas.weeklyschedule.repository;

import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklyScheduleEntity, UUID> {

    Optional<WeeklyScheduleEntity> findByBarberScheduleIdAndDayOfWeek(
            UUID barberScheduleId,
            DayOfWeek dayOfWeek
    );

    List<WeeklyScheduleEntity> findByBarberScheduleIdOrderByDayOfWeek(
            UUID barberScheduleId
    );

    boolean existsByBarberScheduleId(UUID barberScheduleId);
}