package com.barbersaas.barberschedules.repository;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberScheduleRepository extends JpaRepository<BarberScheduleEntity, UUID> {

    /**
     * Busca a configuração de agenda de um barbeiro específico.
     * @param barberId ID do barbeiro
     * @return Configuração de agenda do barbeiro, se existir
     */
    Optional<BarberScheduleEntity> findByBarberId(UUID barberId);

    /**
     * Verifica se um barbeiro possui configuração de agenda.
     * @param barberId ID do barbeiro
     * @return true se o barbeiro possui configuração, false caso contrário
     */
    boolean existsByBarberId(UUID barberId);
}