package com.barbersaas.appointments.dto;

import com.barbersaas.appointments.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class PublicAppointmentResponse {

    private UUID id;
    private UUID customerId;
    private UUID serviceId;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private String cancelToken;

    public PublicAppointmentResponse() {
    }

    public PublicAppointmentResponse(
            UUID id,
            UUID customerId,
            UUID serviceId,
            LocalDateTime appointmentDateTime,
            AppointmentStatus status,
            String notes,
            LocalDateTime createdAt,
            String cancelToken) {
        this.id = id;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.cancelToken = cancelToken;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCancelToken() {
        return cancelToken;
    }

    public void setCancelToken(String cancelToken) {
        this.cancelToken = cancelToken;
    }
}
