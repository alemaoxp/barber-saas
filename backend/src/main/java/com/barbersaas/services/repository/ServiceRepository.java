package com.barbersaas.services.repository;

import com.barbersaas.services.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByBarbershopId(UUID barbershopId);
    List<ServiceEntity> findByBarbershopIdAndActiveTrue(UUID barbershopId);
}
