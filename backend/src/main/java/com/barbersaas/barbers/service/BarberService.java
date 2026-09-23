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
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.barbershops.repository.BarbershopRepository;
import com.barbersaas.exception.NotFoundException;
import java.time.DayOfWeek;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberMapper barberMapper;
    private final BarberScheduleRepository barberScheduleRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final BarbershopRepository barbershopRepository;

    public BarberService(
            BarberRepository barberRepository,
            BarberMapper barberMapper,
            BarberScheduleRepository barberScheduleRepository,
            WeeklyScheduleRepository weeklyScheduleRepository,
            BarbershopRepository barbershopRepository) {
        this.barberRepository = barberRepository;
        this.barberMapper = barberMapper;
        this.barberScheduleRepository = barberScheduleRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.barbershopRepository = barbershopRepository;
    }

    public BarberResponse create(UUID barbershopId, CreateBarberRequest request) {
        BarberEntity entity = barberMapper.toEntity(request);
        entity.setBarbershop(barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new NotFoundException("Barbearia não encontrada.")));
        BarberEntity savedEntity = barberRepository.save(entity);
        
        // Criar configuração de agenda automaticamente para o barbeiro
        createDefaultBarberSchedule(savedEntity);
        
        return barberMapper.toResponse(savedEntity);
    }

    public List<BarberResponse> findAll(UUID barbershopId) {
        return barberRepository.findByBarbershopId(barbershopId)
                .stream()
                .map(barberMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BarberResponse findById(UUID barbershopId, UUID id) {
        return barberRepository.findByIdAndBarbershopId(id, barbershopId)
                .map(barberMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));
    }

    public BarberResponse update(UUID barbershopId, UUID id, UpdateBarberRequest request) {
        BarberEntity entity = barberRepository.findByIdAndBarbershopId(id, barbershopId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));
        
        barberMapper.updateEntity(entity, request);
        BarberEntity updatedEntity = barberRepository.save(entity);
        
        return barberMapper.toResponse(updatedEntity);
    }

    public void delete(UUID barbershopId, UUID id) {
        BarberEntity entity = barberRepository.findByIdAndBarbershopId(id, barbershopId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));
        
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
            BarberScheduleEntity savedSchedule = barberScheduleRepository.save(defaultSchedule);
            System.out.println(">>> Agenda salva.");
            System.out.println(
                    "Encontrou logo após salvar? " +
                            barberScheduleRepository.findByBarberId(barber.getId()).isPresent()
            );
            
            createDefaultWeeklySchedule(savedSchedule);
        }
    }

    private void createDefaultWeeklySchedule(BarberScheduleEntity barberSchedule) {
        List<WeeklyScheduleEntity> weeklySchedules = new ArrayList<>();
        
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            WeeklyScheduleEntity weeklySchedule = new WeeklyScheduleEntity(
                    barberSchedule,
                    dayOfWeek,
                    null,
                    null,
                    false
            );
            weeklySchedules.add(weeklySchedule);
        }
        
        weeklyScheduleRepository.saveAll(weeklySchedules);
    }
}
