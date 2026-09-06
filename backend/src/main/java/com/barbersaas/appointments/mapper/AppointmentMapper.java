package com.barbersaas.appointments.mapper;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.services.entity.ServiceEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AppointmentMapper {
    public AppointmentEntity toEntity(CreateAppointmentRequest request, CustomerEntity customer, BarberEntity barber,
                                      List<ServiceEntity> services, BigDecimal totalPrice) {
        return new AppointmentEntity(customer, barber, services, totalPrice, request.getAppointmentDateTime(),
                AppointmentStatus.SCHEDULED, request.getNotes());
    }

    public void updateEntity(AppointmentEntity entity, UpdateAppointmentRequest request, CustomerEntity customer,
                             List<ServiceEntity> services, BigDecimal totalPrice) {
        entity.setCustomer(customer);
        entity.setServices(services);
        entity.setTotalPrice(totalPrice);
        entity.setAppointmentDateTime(request.getAppointmentDateTime());
        entity.setNotes(request.getNotes());
    }

    public AppointmentResponse toResponse(AppointmentEntity entity) {
        return new AppointmentResponse(entity.getId(), entity.getCustomer().getId(),
                entity.getServices().stream().map(ServiceEntity::getId).toList(), entity.getTotalPrice(),
                entity.getAppointmentDateTime(), entity.getStatus(), entity.getNotes(), entity.getCreatedAt());
    }

    public PublicAppointmentResponse toPublicResponse(AppointmentEntity entity) {
        return new PublicAppointmentResponse(entity.getId(), entity.getCustomer().getId(),
                entity.getServices().stream().map(ServiceEntity::getId).toList(), entity.getTotalPrice(),
                entity.getAppointmentDateTime(), entity.getStatus(), entity.getNotes(), entity.getCreatedAt(),
                entity.getCancelToken());
    }
}
