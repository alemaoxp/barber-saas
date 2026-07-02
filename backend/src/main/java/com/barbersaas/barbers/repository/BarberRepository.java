package com.barbersaas.barbers.repository;

import com.barbersaas.barbers.entity.BarberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BarberRepository extends JpaRepository<BarberEntity, UUID> {
    // No custom methods for now
}