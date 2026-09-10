package com.barbersaas.barbershops.repository;

import com.barbersaas.barbershops.entity.BarbershopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BarbershopRepository extends JpaRepository<BarbershopEntity, UUID> { }
