package com.barbersaas.appointments.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DailyAgendaResponse {

    private UUID barberId;
    private LocalDate date;
    private boolean workingDay;
    private List<DailyAgendaSlotResponse> slots;

    public DailyAgendaResponse() {
    }

    public DailyAgendaResponse(
            UUID barberId,
            LocalDate date,
            boolean workingDay,
            List<DailyAgendaSlotResponse> slots) {
        this.barberId = barberId;
        this.date = date;
        this.workingDay = workingDay;
        this.slots = slots;
    }

    public UUID getBarberId() {
        return barberId;
    }

    public void setBarberId(UUID barberId) {
        this.barberId = barberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public void setWorkingDay(boolean workingDay) {
        this.workingDay = workingDay;
    }

    public List<DailyAgendaSlotResponse> getSlots() {
        return slots;
    }

    public void setSlots(List<DailyAgendaSlotResponse> slots) {
        this.slots = slots;
    }
}
