package com.barbersaas.appointments.mapper;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.mapper.BarberMapper;
import com.barbersaas.customers.dto.CustomerResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.mapper.CustomerMapper;
import com.barbersaas.services.dto.ServiceResponse;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    private final CustomerMapper customerMapper;
    private final BarberMapper barberMapper;
    private final ServiceMapper serviceMapper;

    public AppointmentMapper(CustomerMapper customerMapper, BarberMapper barberMapper, ServiceMapper serviceMapper) {
        this.customerMapper = customerMapper;
        this.barberMapper = barberMapper;
        this.serviceMapper = serviceMapper;
    }

    public AppointmentEntity toEntity(CustomerEntity customer, BarberEntity barber, ServiceEntity service, 
                                    CreateAppointmentRequest request) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setCustomer(customer);
        entity.setBarber(barber);
        entity.setService(service);
        entity.setDate(request.getDate());
        entity.setTime(request.getTime());
        entity.setStatus(request.getStatus());
        return entity;
    }

    public void updateEntity(AppointmentEntity entity, CustomerEntity customer, BarberEntity barber, 
                           ServiceEntity service, UpdateAppointmentRequest request) {
        entity.setCustomer(customer);
        entity.setBarber(barber);
        entity.setService(service);
        entity.setDate(request.getDate());
        entity.setTime(request.getTime());
        entity.setStatus(request.getStatus());
    }

    public AppointmentResponse toResponse(AppointmentEntity entity) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(entity.getId());
        
        CustomerResponse customerResponse = customerMapper.toResponse(entity.getCustomer());
        BarberResponse barberResponse = barberMapper.toResponse(entity.getBarber());
        ServiceResponse serviceResponse = serviceMapper.toResponse(entity.getService());
        
        response.setCustomer(customerResponse);
        response.setBarber(barberResponse);
        response.setService(serviceResponse);
        response.setDate(entity.getDate());
        response.setTime(entity.getTime());
        response.setStatus(entity.getStatus());
        
        return response;
    }
}