package com.barbersaas.availableslot.entity;

import com.barbersaas.availableslot.enums.AvailableSlotStatus;
import com.barbersaas.barbers.entity.BarberEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "available_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_available_slot_barber_datetime",
                        columnNames = {
                                "barber_id",
                                "available_date_time"
                        }
                )
        }
)
public class AvailableSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberEntity barber;

    @Column(name = "available_date_time", nullable = false)
    private LocalDateTime availableDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailableSlotStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AvailableSlotEntity() {
        // Default constructor for JPA
    }

    public AvailableSlotEntity(
            BarberEntity barber,
            LocalDateTime availableDateTime) {

        this.barber = barber;
        this.availableDateTime = availableDateTime;
        this.status = AvailableSlotStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BarberEntity getBarber() {
        return barber;
    }

    public void setBarber(BarberEntity barber) {
        this.barber = barber;
    }

    public LocalDateTime getAvailableDateTime() {
        return availableDateTime;
    }

    public void setAvailableDateTime(
            LocalDateTime availableDateTime) {
        this.availableDateTime = availableDateTime;
    }

    public AvailableSlotStatus getStatus() {
        return status;
    }

    public void setStatus(AvailableSlotStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AvailableSlotEntity{" +
                "id=" + id +
                ", availableDateTime=" + availableDateTime +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}