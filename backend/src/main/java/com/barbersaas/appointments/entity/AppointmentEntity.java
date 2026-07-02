package com.barbersaas.appointments.entity;

import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.services.entity.ServiceEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @NotNull(message = "Barbeiro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberEntity barber;

    @NotNull(message = "Serviço é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @NotNull(message = "Data é obrigatória")
    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "Horário é obrigatório")
    @Column(nullable = false)
    private LocalTime time;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    public AppointmentEntity() {
    }

    public AppointmentEntity(
            CustomerEntity customer,
            BarberEntity barber,
            ServiceEntity service,
            LocalDate date,
            LocalTime time,
            AppointmentStatus status
    ) {
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

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public BarberEntity getBarber() {
        return barber;
    }

    public void setBarber(BarberEntity barber) {
        this.barber = barber;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
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

    @Override
    public String toString() {
        return "AppointmentEntity{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", status=" + status +
                '}';
    }
}