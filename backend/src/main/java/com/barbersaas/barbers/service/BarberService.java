package com.barbersaas.barbers.service;

import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.barbers.dto.UpdateBarberRequest;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.mapper.BarberMapper;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberMapper barberMapper;
    private final BarberScheduleRepository barberScheduleRepository;

    public BarberService(
            BarberRepository barberRepository, 
            BarberMapper barberMapper,
            BarberScheduleRepository barberScheduleRepository) {
        this.barberRepository = barberRepository;
        this.barberMapper = barberMapper;
        this.barberScheduleRepository = barberScheduleRepository;
    }

    public BarberResponse create(CreateBarberRequest request) {
        BarberEntity entity = barberMapper.toEntity(request);
        BarberEntity savedEntity = barberRepository.save(entity);
        
        // Criar configuração de agenda automaticamente para o barbeiro
        createDefaultBarberSchedule(savedEntity);
        
        return barberMapper.toResponse(savedEntity);
    }

    public List<BarberResponse> findAll() {
        return barberRepository.findAll()
                .stream()
                .map(barberMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BarberResponse findById(UUID id) {
        return barberRepository.findById(id)
                .map(barberMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));
    }

    public BarberResponse update(UUID id, UpdateBarberRequest request) {
        BarberEntity entity = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));
        
        barberMapper.updateEntity(entity, request);
        BarberEntity updatedEntity = barberRepository.save(entity);
        
        return barberMapper.toResponse(updatedEntity);
    }

    public void delete(UUID id) {
        BarberEntity entity = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));
        
        barberRepository.delete(entity);
        System.out.println(
        );
    }

    /**
     * Cria uma configuração de agenda padrão para um barbeiro.
     * Verifica se já existe uma configuração antes de criar.
     * @param barber Barbeiro para o qual criar a configuração de agenda
     */
    private void createDefaultBarberSchedule(BarberEntity barber) {
        // Verificar se já existe configuração de agenda para este barbeiro
        boolean scheduleExists = barberScheduleRepository.existsByBarberId(barber.getId());
        
        if (!scheduleExists) {
            // Criar configuração padrão
            BarberScheduleEntity defaultSchedule = new BarberScheduleEntity(
                    barber,
                    30,  // maxBookingDays padrão
                    5    // defaultBreakMinutes padrão
            );
            System.out.println(">>> Criando agenda para barbeiro: " + barber.getId());
            barberScheduleRepository.save(defaultSchedule);
            System.out.println(">>> Agenda salva.");
            System.out.println(
                    "Encontrou logo após salvar? " +
                            barberScheduleRepository.findByBarberId(barber.getId()).isPresent()
            );
        }
    }
}
