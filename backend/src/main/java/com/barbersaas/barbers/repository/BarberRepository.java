package com.barbersaas.barbers.repository;

import com.barbersaas.barbers.entity.BarberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberRepository extends JpaRepository<BarberEntity, UUID> {
    List<BarberEntity> findByBarbershopId(UUID barbershopId);
    Optional<BarberEntity> findByIdAndBarbershopId(UUID id, UUID barbershopId);
}
