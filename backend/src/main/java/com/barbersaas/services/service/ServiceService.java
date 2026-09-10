package com.barbersaas.services.service;

import com.barbersaas.services.dto.CreateServiceRequest;
import com.barbersaas.services.dto.UpdateServiceRequest;
import com.barbersaas.services.dto.ServiceResponse;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import com.barbersaas.services.repository.ServiceRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final BarberRepository barberRepository;

    public ServiceService(ServiceRepository serviceRepository, ServiceMapper serviceMapper, BarberRepository barberRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
        this.barberRepository = barberRepository;
    }

    public ServiceResponse create(UUID barberId, CreateServiceRequest request) {
        ServiceEntity entity = serviceMapper.toEntity(request);
        entity.setBarbershop(barber(barberId).getBarbershop());
        ServiceEntity savedEntity = serviceRepository.save(entity);
        return serviceMapper.toResponse(savedEntity);
    }

    public List<ServiceResponse> findAll(UUID barberId) {
        return serviceRepository.findByBarbershopId(barber(barberId).getBarbershop().getId())
                .stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ServiceResponse> findActive(UUID barberId) {
        return serviceRepository.findByBarbershopIdAndActiveTrue(barber(barberId).getBarbershop().getId())
                .stream()
                .filter(service -> Boolean.TRUE.equals(service.getActive()))
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ServiceResponse findById(UUID barberId, UUID id) {
        return serviceRepository.findById(id)
                .filter(service -> service.getBarbershop().getId().equals(barber(barberId).getBarbershop().getId()))
                .map(serviceMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));
    }

    public ServiceResponse update(UUID barberId, UUID id, UpdateServiceRequest request) {
        ServiceEntity entity = serviceRepository.findById(id)
                .filter(service -> service.getBarbershop().getId().equals(barber(barberId).getBarbershop().getId()))
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));
        
        serviceMapper.updateEntity(entity, request);
        ServiceEntity updatedEntity = serviceRepository.save(entity);
        
        return serviceMapper.toResponse(updatedEntity);
    }

    public void delete(UUID barberId, UUID id) {
        ServiceEntity entity = serviceRepository.findById(id)
                .filter(service -> service.getBarbershop().getId().equals(barber(barberId).getBarbershop().getId()))
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));
        
        serviceRepository.delete(entity);
    }

    private BarberEntity barber(UUID barberId) {
        return barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));
    }
}
