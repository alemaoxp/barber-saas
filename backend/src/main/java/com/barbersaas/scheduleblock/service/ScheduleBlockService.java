package com.barbersaas.scheduleblock.service;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.scheduleblock.dto.CreateScheduleBlockRequest;
import com.barbersaas.scheduleblock.dto.ScheduleBlockResponse;
import com.barbersaas.scheduleblock.dto.UpdateScheduleBlockRequest;
import com.barbersaas.scheduleblock.entity.ScheduleBlockEntity;
import com.barbersaas.scheduleblock.mapper.ScheduleBlockMapper;
import com.barbersaas.scheduleblock.repository.ScheduleBlockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScheduleBlockService {

    private final ScheduleBlockRepository scheduleBlockRepository;
    private final BarberScheduleRepository barberScheduleRepository;
    private final ScheduleBlockMapper scheduleBlockMapper;

    public ScheduleBlockService(
            ScheduleBlockRepository scheduleBlockRepository,
            BarberScheduleRepository barberScheduleRepository,
            ScheduleBlockMapper scheduleBlockMapper) {
        this.scheduleBlockRepository = scheduleBlockRepository;
        this.barberScheduleRepository = barberScheduleRepository;
        this.scheduleBlockMapper = scheduleBlockMapper;
    }

    private BarberScheduleEntity findBarberSchedule(UUID barberId) {
        return barberScheduleRepository.findByBarberId(barberId)
                .orElseThrow(() -> new RuntimeException("Barber schedule not found."));
    }

    private ScheduleBlockEntity findScheduleBlock(UUID scheduleBlockId, BarberScheduleEntity barberSchedule) {
        return scheduleBlockRepository
                .findByIdAndBarberSchedule(scheduleBlockId, barberSchedule)
                .orElseThrow(() -> new RuntimeException("Schedule block not found."));
    }

    public ScheduleBlockResponse create(UUID barberId, CreateScheduleBlockRequest request) {
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);
        
        ScheduleBlockEntity entity = scheduleBlockMapper.toEntity(request, barberSchedule);
        ScheduleBlockEntity savedEntity = scheduleBlockRepository.save(entity);
        
        return scheduleBlockMapper.toResponse(savedEntity);
    }

    public List<ScheduleBlockResponse> findAll(UUID barberId) {
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);
        
        List<ScheduleBlockEntity> scheduleBlocks = scheduleBlockRepository.findByBarberSchedule(barberSchedule);
        
        return scheduleBlocks.stream()
                .map(scheduleBlockMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduleBlockResponse findById(UUID barberId, UUID scheduleBlockId) {
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);
        ScheduleBlockEntity scheduleBlock = findScheduleBlock(scheduleBlockId, barberSchedule);
        
        return scheduleBlockMapper.toResponse(scheduleBlock);
    }

    public ScheduleBlockResponse update(UUID barberId, UUID scheduleBlockId, UpdateScheduleBlockRequest request) {
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);
        ScheduleBlockEntity scheduleBlock = findScheduleBlock(scheduleBlockId, barberSchedule);
        
        scheduleBlockMapper.updateEntity(scheduleBlock, request);
        ScheduleBlockEntity savedEntity = scheduleBlockRepository.save(scheduleBlock);
        
        return scheduleBlockMapper.toResponse(savedEntity);
    }

    public void delete(UUID barberId, UUID scheduleBlockId) {
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);
        ScheduleBlockEntity scheduleBlock = findScheduleBlock(scheduleBlockId, barberSchedule);
        
        scheduleBlockRepository.delete(scheduleBlock);
    }
}
