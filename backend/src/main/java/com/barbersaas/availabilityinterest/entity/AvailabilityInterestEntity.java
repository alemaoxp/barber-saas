package com.barbersaas.availabilityinterest.entity;

import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.customers.entity.CustomerEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "availability_interests")
public class AvailabilityInterestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityInterestStatus status;

    public AvailabilityInterestEntity() {
        // Default constructor for JPA
    }

    public AvailabilityInterestEntity(
            CustomerEntity customer,
            AppointmentEntity appointment) {

        this.customer = customer;
        this.appointment = appointment;
        this.createdAt = LocalDateTime.now();
        this.status = AvailabilityInterestStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public AppointmentEntity getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentEntity appointment) {
        this.appointment = appointment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AvailabilityInterestStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityInterestStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AvailabilityInterestEntity{" +
                "id=" + id +
                ", customer=" + customer +
                ", appointment=" + appointment +
                ", createdAt=" + createdAt +
                '}';
    }
}