package com.barbersaas.scheduleblock.entity;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_blocks")
public class ScheduleBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_schedule_id", nullable = false)
    private BarberScheduleEntity barberSchedule;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ScheduleBlockEntity() {
    }

    public ScheduleBlockEntity(
            BarberScheduleEntity barberSchedule,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reason) {
        this.barberSchedule = barberSchedule;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BarberScheduleEntity getBarberSchedule() {
        return barberSchedule;}

    public void setBarberSchedule(BarberScheduleEntity barberSchedule) {
        this.barberSchedule = barberSchedule;
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
        return "ScheduleBlockEntity{" +
                "id=" + id +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}