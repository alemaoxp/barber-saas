package com.barbersaas.scheduleblock.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduleBlockResponse {

    private UUID id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;
    private LocalDateTime createdAt;

    public ScheduleBlockResponse() {
    }

    public ScheduleBlockResponse(
            UUID id,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reason,
            LocalDateTime createdAt) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.reason = reason;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ScheduleBlockResponse{" +
                "id=" + id +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}