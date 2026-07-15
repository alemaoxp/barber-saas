package com.barbersaas.scheduleblock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateScheduleBlockRequest {

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @Size(max = 255)
    private String reason;

    public CreateScheduleBlockRequest() {
    }

    public CreateScheduleBlockRequest(LocalDateTime startDateTime, LocalDateTime endDateTime, String reason) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.reason = reason;
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

    @Override
    public String toString() {
        return "CreateScheduleBlockRequest{" +
                "startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", reason='" + reason + '\'' +
                '}';
    }
}