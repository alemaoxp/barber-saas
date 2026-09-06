package com.barbersaas.appointments.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreateAppointmentRequest {
    @NotNull private UUID customerId;
    @NotEmpty private List<@NotNull UUID> serviceIds;
    @NotNull private LocalDateTime appointmentDateTime;
    @Size(max = 255) private String notes;

    public CreateAppointmentRequest() {}

    public CreateAppointmentRequest(UUID customerId, List<UUID> serviceIds,
                                    LocalDateTime appointmentDateTime, String notes) {
        this.customerId = customerId;
        this.serviceIds = serviceIds;
        this.appointmentDateTime = appointmentDateTime;
        this.notes = notes;
    }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public List<UUID> getServiceIds() { return serviceIds; }
    public void setServiceIds(List<UUID> serviceIds) { this.serviceIds = serviceIds; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
