package com.barbersaas.availabilityinterest.dto;

import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AvailabilityInterestResponse {

    private UUID id;
    private UUID customerId;
    private UUID appointmentId;
    private LocalDateTime createdAt;
    private AvailabilityInterestStatus status;

    public AvailabilityInterestResponse() {
    }

    public AvailabilityInterestResponse(
            UUID id,
            UUID customerId,
            UUID appointmentId,
            LocalDateTime createdAt,
            AvailabilityInterestStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.appointmentId = appointmentId;
        this.createdAt = createdAt;
        this.status = status;
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

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AvailabilityInterestStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityInterestStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AvailabilityInterestResponse{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", appointmentId=" + appointmentId +
                ", createdAt=" + createdAt +
                ", status=" + status +
                '}';
    }
}