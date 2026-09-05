package com.barbersaas.availabilityinterest.repository;

import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityInterestRepository
        extends JpaRepository<AvailabilityInterestEntity, UUID> {

    List<AvailabilityInterestEntity> findByStatusOrderByCreatedAtAsc(
            AvailabilityInterestStatus status
    );

    List<AvailabilityInterestEntity> findByCustomerIdAndStatus(
            UUID customerId,
            AvailabilityInterestStatus status
    );

    boolean existsByCustomerIdAndAppointmentIdAndStatus(
            UUID customerId,
            UUID appointmentId,
            AvailabilityInterestStatus status
    );

    @Query("""
            SELECT ai
            FROM AvailabilityInterestEntity ai
            WHERE ai.status = :status
            AND ai.appointment.barber.id = :barberId
            AND ai.appointment.appointmentDateTime > :availableDateTime
            """)
    List<AvailabilityInterestEntity> findEligibleInterests(
            @Param("status") AvailabilityInterestStatus status,
            @Param("barberId") UUID barberId,
            @Param("availableDateTime") LocalDateTime availableDateTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ai
            FROM AvailabilityInterestEntity ai
            WHERE ai.id = :interestId
            """)
    Optional<AvailabilityInterestEntity> findByIdForUpdate(
            @Param("interestId") UUID interestId
    );
}