package com.barbersaas.appointments.repository;

import com.barbersaas.appointments.entity.AppointmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    Optional<AppointmentEntity> findByIdAndBarberId(
            UUID appointmentId,
            UUID barberId
    );

    List<AppointmentEntity> findByBarberId(
            UUID barberId
    );

    List<AppointmentEntity> findByCustomerIdOrderByAppointmentDateTimeDesc(
            UUID customerId
    );

    List<AppointmentEntity> findByBarberIdAndAppointmentDateTimeBetween(
            UUID barberId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<AppointmentEntity> findByCancelToken(
            String cancelToken
    );

    boolean existsByBarberIdAndAppointmentDateTime(
            UUID barberId,
            LocalDateTime appointmentDateTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM AppointmentEntity a
            WHERE a.barber.id = :barberId
            AND a.appointmentDateTime = :appointmentDateTime
            """)
    Optional<AppointmentEntity> findByBarberIdAndAppointmentDateTimeForUpdate(
            @Param("barberId") UUID barberId,
            @Param("appointmentDateTime") LocalDateTime appointmentDateTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a
        FROM AppointmentEntity a
        WHERE a.id = :appointmentId
        """)
    Optional<AppointmentEntity> findByIdForUpdate(
            @Param("appointmentId") UUID appointmentId
    );
}
