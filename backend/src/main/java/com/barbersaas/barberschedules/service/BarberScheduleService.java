package com.barbersaas.barberschedules.service;

import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barberschedules.dto.BarberScheduleRequest;
import com.barbersaas.barberschedules.dto.BarberScheduleResponse;
import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.mapper.BarberScheduleMapper;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class BarberScheduleService {

    private final BarberScheduleRepository barberScheduleRepository;
    private final BarberRepository barberRepository;
    private final BarberScheduleMapper barberScheduleMapper;

    public BarberScheduleService(
            BarberScheduleRepository barberScheduleRepository,
            BarberRepository barberRepository,
            BarberScheduleMapper barberScheduleMapper) {
        this.barberScheduleRepository = barberScheduleRepository;
        this.barberRepository = barberRepository;
        this.barberScheduleMapper = barberScheduleMapper;
    }

    /**
     * Obtém a configuração de agenda de um barbeiro específico.
     * @param barberId ID do barbeiro
     * @return Configuração de agenda do barbeiro
     * @throws RuntimeException se o barbeiro não existir ou não possuir configuração
     */
    public BarberScheduleResponse getByBarberId(UUID barberId) {
        // Verificar se o barbeiro existe
        BarberEntity barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));

        // Buscar configuração de agenda do barbeiro
        BarberScheduleEntity schedule = barberScheduleRepository.findByBarberId(barberId)
                .orElseThrow(() -> new RuntimeException("Configuração de agenda não encontrada para este barbeiro."));

        return barberScheduleMapper.toResponse(schedule);
    }

    /**
     * Atualiza a configuração de agenda de um barbeiro.
     * @param barberId ID do barbeiro
     * @param request DTO com os novos dados da configuração
     * @return Configuração atualizada
     * @throws RuntimeException se o barbeiro não existir ou não possuir configuração
     */
    public BarberScheduleResponse update(UUID barberId, BarberScheduleRequest request) {
        // Verificar se o barbeiro existe
        BarberEntity barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));

        // Buscar configuração de agenda do barbeiro
        BarberScheduleEntity schedule = barberScheduleRepository.findByBarberId(barberId)
                .orElseThrow(() -> new RuntimeException("Configuração de agenda não encontrada para este barbeiro."));

        // Atualizar entidade com os dados do request
        barberScheduleMapper.updateEntity(schedule, request);

        // Salvar entidade atualizada
        BarberScheduleEntity updatedSchedule = barberScheduleRepository.save(schedule);

        return barberScheduleMapper.toResponse(updatedSchedule);
    }
}