package com.barbersaas.appointments.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class DashboardSummaryResponse {
    private UUID barberId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int appointmentCount;
    private BigDecimal scheduledValue;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(
            UUID barberId,
            LocalDate startDate,
            LocalDate endDate,
            int appointmentCount,
            BigDecimal scheduledValue) {
        this.barberId = barberId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appointmentCount = appointmentCount;
        this.scheduledValue = scheduledValue;
    }

    public UUID getBarberId() { return barberId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getAppointmentCount() { return appointmentCount; }
    public BigDecimal getScheduledValue() { return scheduledValue; }
}
