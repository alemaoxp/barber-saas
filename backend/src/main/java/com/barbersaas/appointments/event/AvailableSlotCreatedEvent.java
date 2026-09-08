package com.barbersaas.appointments.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvailableSlotCreatedEvent(
        UUID barberId,
        LocalDateTime availableDateTime) {
}
