package com.barbersaas.availabilityinterest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AvailabilityOpportunityResponse {

    private UUID availableSlotId;
    private LocalDateTime availableDateTime;

    public AvailabilityOpportunityResponse() {
    }

    public AvailabilityOpportunityResponse(
            UUID availableSlotId,
            LocalDateTime availableDateTime) {
        this.availableSlotId = availableSlotId;
        this.availableDateTime = availableDateTime;
    }

    public UUID getAvailableSlotId() {
        return availableSlotId;
    }

    public void setAvailableSlotId(UUID availableSlotId) {
        this.availableSlotId = availableSlotId;
    }

    public LocalDateTime getAvailableDateTime() {
        return availableDateTime;
    }

    public void setAvailableDateTime(LocalDateTime availableDateTime) {
        this.availableDateTime = availableDateTime;
    }
}
