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

@Component
public class AppointmentMapper {

    public AppointmentEntity toEntity(
            CreateAppointmentRequest request,
            CustomerEntity customer,
            BarberEntity barber,
            ServiceEntity service) {
        return new AppointmentEntity(
                customer,
                barber,
                service,
                request.getAppointmentDateTime(),
                AppointmentStatus.SCHEDULED,
                request.getNotes()
        );
    }

    public void updateEntity(
            AppointmentEntity entity,
            UpdateAppointmentRequest request,
            CustomerEntity customer,
            ServiceEntity service) {
        entity.setCustomer(customer);
        entity.setService(service);
        entity.setAppointmentDateTime(request.getAppointmentDateTime());
        entity.setNotes(request.getNotes());
    }

    public AppointmentResponse toResponse(AppointmentEntity entity) {
        return new AppointmentResponse(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getService().getId(),
                entity.getAppointmentDateTime(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt()
        );
    }

    public PublicAppointmentResponse toPublicResponse(AppointmentEntity entity) {
        return new PublicAppointmentResponse(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getService().getId(),
                entity.getAppointmentDateTime(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getCancelToken()
        );
    }
}
