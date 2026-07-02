package com.barbersaas.barbers.service;

import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.barbers.dto.UpdateBarberRequest;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.mapper.BarberMapper;
import com.barbersaas.barbers.repository.BarberRepository;
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

    public BarberService(BarberRepository barberRepository, BarberMapper barberMapper) {
        this.barberRepository = barberRepository;
        this.barberMapper = barberMapper;
    }

    public BarberResponse create(CreateBarberRequest request) {
        BarberEntity entity = barberMapper.toEntity(request);
        BarberEntity savedEntity = barberRepository.save(entity);
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
    }
}