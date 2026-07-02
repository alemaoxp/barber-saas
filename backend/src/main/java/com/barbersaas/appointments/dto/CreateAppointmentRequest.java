package com.barbersaas.appointments.dto;

import com.barbersaas.appointments.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class CreateAppointmentRequest {

    @NotNull(message = "ID do cliente é obrigatório")
    private UUID customerId;

    @NotNull(message = "ID do barbeiro é obrigatório")
    private UUID barberId;

    @NotNull(message = "ID do serviço é obrigatório")
    private UUID serviceId;

    @NotNull(message = "Data é obrigatória")
    private LocalDate date;

    @NotNull(message = "Horário é obrigatório")
    private LocalTime time;

    @NotNull(message = "Status é obrigatório")
    private AppointmentStatus status;

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getBarberId() {
        return barberId;
    }

    public void setBarberId(UUID barberId) {
        this.barberId = barberId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}