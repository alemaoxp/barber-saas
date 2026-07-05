package com.barbersaas.barberschedules.entity;

import com.barbersaas.barbers.entity.BarberEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Representa a configuração principal da agenda de um barbeiro.
 * Esta entidade armazena apenas configurações globais da agenda.
 * Os horários semanais e bloqueios serão representados por entidades específicas.
 */
@Entity
@Table(name = "barber_schedules")
public class BarberScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Barbeiro proprietário desta configuração de agenda.
     * Relacionamento 1:1 - cada barbeiro possui exatamente uma configuração.
     */
    @NotNull(message = "Barbeiro é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false, unique = true)
    private BarberEntity barber;

    /**
     * Quantidade máxima de dias futuros permitidos para realização de novos agendamentos.
     * Exemplo: valor 30 significa que clientes podem agendar até 30 dias no futuro.
     */
    @NotNull(message = "Dias máximos para agendamento é obrigatório")
    @Positive(message = "Dias máximos para agendamento deve ser positivo")
    @Column(name = "max_booking_days", nullable = false)
    private Integer maxBookingDays;

    /**
     * Intervalo padrão, em minutos, entre dois atendimentos consecutivos.
     * Este tempo é utilizado para evitar sobreposição de agendamentos
     * e permitir preparação entre clientes.
     */
    @NotNull(message = "Intervalo padrão entre clientes é obrigatório")
    @Positive(message = "Intervalo padrão entre clientes deve ser positivo")
    @Column(name = "default_break_minutes", nullable = false)
    private Integer defaultBreakMinutes;

    public BarberScheduleEntity() {
    }

    public BarberScheduleEntity(BarberEntity barber, Integer maxBookingDays, Integer defaultBreakMinutes) {
        this.barber = barber;
        this.maxBookingDays = maxBookingDays;
        this.defaultBreakMinutes = defaultBreakMinutes;
    }

    public UUID getId() {
        return id;
    }
    public BarberEntity getBarber() {
        return barber;
    }

    public void setBarber(BarberEntity barber) {
        this.barber = barber;
    }

    public Integer getMaxBookingDays() {
        return maxBookingDays;   }

    public void setMaxBookingDays(Integer maxBookingDays) {
        this.maxBookingDays = maxBookingDays;
    }

    public Integer getDefaultBreakMinutes() {
        return defaultBreakMinutes;
    }

    public void setDefaultBreakMinutes(Integer defaultBreakMinutes) {
        this.defaultBreakMinutes = defaultBreakMinutes;
    }

    @Override
    public String toString() {
        return "BarberScheduleEntity{" +
                "id=" + id +
                ", maxBookingDays=" + maxBookingDays +
                ", defaultBreakMinutes=" + defaultBreakMinutes +
                '}';
    }
}