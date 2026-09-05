package com.barbersaas.availabilityinterest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AcceptAvailabilityInterestRequest {

    @NotNull(message = "Vaga de antecipação é obrigatória.")
    private UUID availableSlotId;

    public AcceptAvailabilityInterestRequest() {
    }

    public AcceptAvailabilityInterestRequest(UUID availableSlotId) {
        this.availableSlotId = availableSlotId;
    }

    public UUID getAvailableSlotId() {
        return availableSlotId;
    }

    public void setAvailableSlotId(UUID availableSlotId) {
        this.availableSlotId = availableSlotId;
    }
}