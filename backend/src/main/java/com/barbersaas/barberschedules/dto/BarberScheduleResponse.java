package com.barbersaas.barberschedules.dto;

import java.util.UUID;

public class BarberScheduleResponse {

    private UUID id;
    private UUID barberId;
    private Integer maxBookingDays;
    private Integer defaultBreakMinutes;

    public BarberScheduleResponse() {
    }

    public BarberScheduleResponse(UUID id, UUID barberId, Integer maxBookingDays, Integer defaultBreakMinutes) {
        this.id = id;
        this.barberId = barberId;
        this.maxBookingDays = maxBookingDays;
        this.defaultBreakMinutes = defaultBreakMinutes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBarberId() {
        return barberId;
    }

    public void setBarberId(UUID barberId) {
        this.barberId = barberId;
    }

    public Integer getMaxBookingDays() {
        return maxBookingDays;
    }

    public void setMaxBookingDays(Integer maxBookingDays) {
        this.maxBookingDays = maxBookingDays;
    }

    public Integer getDefaultBreakMinutes() {
        return defaultBreakMinutes;
    }

    public void setDefaultBreakMinutes(Integer defaultBreakMinutes) {
        this.defaultBreakMinutes = defaultBreakMinutes;
    }
}