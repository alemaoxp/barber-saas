package com.barbersaas.services.service;

import com.barbersaas.services.dto.CreateServiceRequest;
import com.barbersaas.services.dto.UpdateServiceRequest;
import com.barbersaas.services.dto.ServiceResponse;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import com.barbersaas.services.repository.ServiceRepository;
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

    public ServiceService(ServiceRepository serviceRepository, ServiceMapper serviceMapper) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
    }

    public ServiceResponse create(CreateServiceRequest request) {
        ServiceEntity entity = serviceMapper.toEntity(request);
        ServiceEntity savedEntity = serviceRepository.save(entity);
        return serviceMapper.toResponse(savedEntity);
    }

    public List<ServiceResponse> findAll() {
        return serviceRepository.findAll()
                .stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ServiceResponse findById(UUID id) {
        return serviceRepository.findById(id)
                .map(serviceMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));
    }

    public ServiceResponse update(UUID id, UpdateServiceRequest request) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));
        
        serviceMapper.updateEntity(entity, request);
        ServiceEntity updatedEntity = serviceRepository.save(entity);
        
        return serviceMapper.toResponse(updatedEntity);
    }

    public void delete(UUID id) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));
        
        serviceRepository.delete(entity);
    }
}
