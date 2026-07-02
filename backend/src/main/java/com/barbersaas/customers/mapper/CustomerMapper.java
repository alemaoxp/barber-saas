package com.barbersaas.customers.mapper;

import com.barbersaas.customers.dto.CreateCustomerRequest;
import com.barbersaas.customers.dto.UpdateCustomerRequest;
import com.barbersaas.customers.dto.CustomerResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerEntity toEntity(CreateCustomerRequest request) {
        CustomerEntity entity = new CustomerEntity();
        entity.setName(request.getName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setBirthDate(request.getBirthDate());
        entity.setNotes(request.getNotes());
        entity.setActive(request.getActive());
        return entity;
    }

    public void updateEntity(CustomerEntity entity, UpdateCustomerRequest request) {
        entity.setName(request.getName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setBirthDate(request.getBirthDate());
        entity.setNotes(request.getNotes());
        entity.setActive(request.getActive());
    }

    public CustomerResponse toResponse(CustomerEntity entity) {
        CustomerResponse response = new CustomerResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setPhone(entity.getPhone());
        response.setEmail(entity.getEmail());
        response.setBirthDate(entity.getBirthDate());
        response.setNotes(entity.getNotes());
        response.setActive(entity.getActive());
        return response;
    }
}