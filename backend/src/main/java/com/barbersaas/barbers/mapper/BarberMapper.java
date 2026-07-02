package com.barbersaas.barbers.mapper;

import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.barbers.dto.UpdateBarberRequest;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.entity.BarberEntity;
import org.springframework.stereotype.Component;

@Component
public class BarberMapper {

    public BarberEntity toEntity(CreateBarberRequest request) {
        BarberEntity entity = new BarberEntity();
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setSpecialties(request.getSpecialties());
        entity.setActive(request.getActive());
        return entity;
    }

    public void updateEntity(BarberEntity entity, UpdateBarberRequest request) {
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setSpecialties(request.getSpecialties());
        entity.setActive(request.getActive());
    }

    public BarberResponse toResponse(BarberEntity entity) {
        BarberResponse response = new BarberResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setPhone(entity.getPhone());
        response.setSpecialties(entity.getSpecialties());
        response.setActive(entity.getActive());
        return response;
    }
}