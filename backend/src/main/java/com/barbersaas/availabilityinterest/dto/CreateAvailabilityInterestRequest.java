package com.barbersaas.availabilityinterest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateAvailabilityInterestRequest {

    @NotNull(message = "Agendamento é obrigatório")
    private UUID appointmentId;

    public CreateAvailabilityInterestRequest() {
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }
}