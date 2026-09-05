package com.barbersaas.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateAppointmentRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID serviceId;

    @NotNull
    private LocalDateTime appointmentDateTime;

    @Size(max = 255)
    private String notes;

    public UpdateAppointmentRequest() {
    }

    public UpdateAppointmentRequest(UUID customerId, UUID serviceId, LocalDateTime appointmentDateTime, String notes) {
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.appointmentDateTime = appointmentDateTime;
        this.notes = notes;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateAppointmentRequest{" +
                "customerId=" + customerId +
                ", serviceId=" + serviceId +
                ", appointmentDateTime=" + appointmentDateTime +
                ", notes='" + notes + '\'' +
                '}';
    }
}