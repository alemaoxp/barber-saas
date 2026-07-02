package com.barbersaas.appointments.dto;

import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.customers.dto.CustomerResponse;
import com.barbersaas.services.dto.ServiceResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class AppointmentResponse {

    private UUID id;
    private CustomerResponse customer;
    private BarberResponse barber;
    private ServiceResponse service;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status;

    public AppointmentResponse() {
    }

    public AppointmentResponse(
            UUID id,
            CustomerResponse customer,
            BarberResponse barber,
            ServiceResponse service,
            LocalDate date,
            LocalTime time,
            AppointmentStatus status
    ) {
        this.id = id;
        this.customer = customer;
        this.barber = barber;
        this.service = service;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerResponse customer) {
        this.customer = customer;
    }

    public BarberResponse getBarber() {
        return barber;
    }

    public void setBarber(BarberResponse barber) {
        this.barber = barber;
    }

    public ServiceResponse getService() {
        return service;
    }

    public void setService(ServiceResponse service) {
        this.service = service;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}