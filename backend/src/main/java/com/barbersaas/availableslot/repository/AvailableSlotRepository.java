package com.barbersaas.availableslot.repository;

import com.barbersaas.availableslot.entity.AvailableSlotEntity;
import com.barbersaas.availableslot.enums.AvailableSlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailableSlotRepository
        extends JpaRepository<AvailableSlotEntity, UUID> {

    Optional<AvailableSlotEntity> findByBarberIdAndAvailableDateTime(
            UUID barberId,
            LocalDateTime availableDateTime
    );

    Optional<AvailableSlotEntity> findByIdAndStatus(
            UUID id,
            AvailableSlotStatus status
    );

    List<AvailableSlotEntity> findByBarberIdAndStatusAndAvailableDateTimeBetweenOrderByAvailableDateTimeAsc(
            UUID barberId,
            AvailableSlotStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM AvailableSlotEntity s
            WHERE s.id = :id
            """)
    Optional<AvailableSlotEntity> findByIdForUpdate(
            @Param("id") UUID id
    );
}
