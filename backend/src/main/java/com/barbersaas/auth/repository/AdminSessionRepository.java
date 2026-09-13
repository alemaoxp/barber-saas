package com.barbersaas.auth.repository;

import com.barbersaas.auth.entity.AdminSessionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminSessionRepository extends JpaRepository<AdminSessionEntity, UUID> {
    @EntityGraph(attributePaths = {"adminUser", "adminUser.barbershop"})
    Optional<AdminSessionEntity> findByTokenHash(String tokenHash);
}
