package com.barbersaas.appointments.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class DailyAgendaBlockSummary {

    private UUID id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;

    public DailyAgendaBlockSummary() {
    }

    public DailyAgendaBlockSummary(
            UUID id,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reason) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.reason = reason;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
