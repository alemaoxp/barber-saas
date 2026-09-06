package com.barbersaas.appointments.dto;

import com.barbersaas.appointments.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AppointmentResponse {
    private UUID id;
    private UUID customerId;
    private List<UUID> serviceIds;
    private BigDecimal totalPrice;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;

    public AppointmentResponse() {}

    public AppointmentResponse(UUID id, UUID customerId, List<UUID> serviceIds, BigDecimal totalPrice,
                               LocalDateTime appointmentDateTime, AppointmentStatus status,
                               String notes, LocalDateTime createdAt) {
        this.id = id; this.customerId = customerId; this.serviceIds = serviceIds; this.totalPrice = totalPrice;
        this.appointmentDateTime = appointmentDateTime; this.status = status; this.notes = notes; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public List<UUID> getServiceIds() { return serviceIds; }
    public void setServiceIds(List<UUID> serviceIds) { this.serviceIds = serviceIds; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
