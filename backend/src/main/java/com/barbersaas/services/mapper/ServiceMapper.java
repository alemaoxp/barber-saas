package com.barbersaas.services.mapper;

import com.barbersaas.services.dto.CreateServiceRequest;
import com.barbersaas.services.dto.UpdateServiceRequest;
import com.barbersaas.services.dto.ServiceResponse;
import com.barbersaas.services.entity.ServiceEntity;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceEntity toEntity(CreateServiceRequest request) {
        ServiceEntity entity = new ServiceEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDurationMinutes(request.getDurationMinutes());
        entity.setPrice(request.getPrice());
        entity.setActive(request.getActive());
        return entity;
    }

    public void updateEntity(ServiceEntity entity, UpdateServiceRequest request) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDurationMinutes(request.getDurationMinutes());
        entity.setPrice(request.getPrice());
        entity.setActive(request.getActive());
    }

    public ServiceResponse toResponse(ServiceEntity entity) {
        ServiceResponse response = new ServiceResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setDurationMinutes(entity.getDurationMinutes());
        response.setPrice(entity.getPrice());
        response.setActive(entity.getActive());
        return response;
    }
}
