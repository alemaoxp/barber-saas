package com.barbersaas.appointments.dto;

import com.barbersaas.appointments.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DailyAgendaSlotResponse {

    private LocalDateTime dateTime;
    private DailyAgendaSlotStatus status;
    private UUID appointmentId;
    private AppointmentStatus appointmentStatus;
    private DailyAgendaCustomerSummary customer;
    private List<DailyAgendaServiceSummary> services;
    private BigDecimal totalPrice;
    private DailyAgendaBlockSummary block;

    public DailyAgendaSlotResponse() {
    }

    private DailyAgendaSlotResponse(
            LocalDateTime dateTime,
            DailyAgendaSlotStatus status) {
        this.dateTime = dateTime;
        this.status = status;
    }

    public static DailyAgendaSlotResponse free(LocalDateTime dateTime) {
        return new DailyAgendaSlotResponse(dateTime, DailyAgendaSlotStatus.FREE);
    }

    public static DailyAgendaSlotResponse occupied(
            LocalDateTime dateTime,
            UUID appointmentId,
            AppointmentStatus appointmentStatus,
            DailyAgendaCustomerSummary customer,
            List<DailyAgendaServiceSummary> services,
            BigDecimal totalPrice) {

        DailyAgendaSlotResponse response =
                new DailyAgendaSlotResponse(dateTime, DailyAgendaSlotStatus.OCCUPIED);
        response.appointmentId = appointmentId;
        response.appointmentStatus = appointmentStatus;
        response.customer = customer;
        response.services = services;
        response.totalPrice = totalPrice;
        return response;
    }

    public static DailyAgendaSlotResponse blocked(
            LocalDateTime dateTime,
            DailyAgendaBlockSummary block) {

        DailyAgendaSlotResponse response =
                new DailyAgendaSlotResponse(dateTime, DailyAgendaSlotStatus.BLOCKED);
        response.block = block;
        return response;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public DailyAgendaSlotStatus getStatus() {
        return status;
    }

    public void setStatus(DailyAgendaSlotStatus status) {
        this.status = status;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public DailyAgendaCustomerSummary getCustomer() {
        return customer;
    }

    public void setCustomer(DailyAgendaCustomerSummary customer) {
        this.customer = customer;
    }

    public List<DailyAgendaServiceSummary> getServices() {
        return services;
    }

    public void setServices(List<DailyAgendaServiceSummary> services) {
        this.services = services;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public DailyAgendaBlockSummary getBlock() {
        return block;
    }

    public void setBlock(DailyAgendaBlockSummary block) {
        this.block = block;
    }
}
