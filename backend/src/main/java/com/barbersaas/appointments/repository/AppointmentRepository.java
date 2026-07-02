package com.barbersaas.appointments.repository;

import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.barbers.entity.BarberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    boolean existsByBarberAndDateAndTime(BarberEntity barber, LocalDate date, LocalTime time);
}