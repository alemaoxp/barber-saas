package com.barbersaas.weeklyschedule.entity;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "weekly_schedule",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_weekly_schedule_barber_schedule_day",
            columnNames = {"barber_schedule_id", "day_of_week"}
        )
    }
)
public class WeeklyScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "BarberSchedule é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_schedule_id", nullable = false)
    private BarberScheduleEntity barberSchedule;

    @NotNull(message = "Dia da semana é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "working_day", nullable = false)
    private boolean workingDay;

    public WeeklyScheduleEntity() {
    }

    public WeeklyScheduleEntity(
            BarberScheduleEntity barberSchedule,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            boolean workingDay) {
        this.barberSchedule = barberSchedule;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workingDay = workingDay;
    }

    public UUID getId() {
        return id;
    }

    public BarberScheduleEntity getBarberSchedule() {
        return barberSchedule;
    }

    public void setBarberSchedule(BarberScheduleEntity barberSchedule) {
        this.barberSchedule = barberSchedule;
}

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public void setWorkingDay(boolean workingDay) {
        this.workingDay = workingDay;
    }

    @Override
    public String toString() {
        return "WeeklyScheduleEntity{" +
                "id=" + id +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", workingDay=" + workingDay +
                '}';
    }
}