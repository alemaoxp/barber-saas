package com.barbersaas.barberschedules.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BarberScheduleRequest {

    /**
     * Quantidade máxima de dias futuros permitidos para realização de novos agendamentos.
     * Exemplo: valor 30 significa que clientes podem agendar até 30 dias no futuro.
     */
    @NotNull(message = "Dias máximos para agendamento é obrigatório")
    @Positive(message = "Dias máximos para agendamento deve ser positivo")
    private Integer maxBookingDays;

    /**
     * Intervalo padrão, em minutos, entre dois atendimentos consecutivos.
     * Este tempo é utilizado para evitar sobreposição de agendamentos
     * e permitir preparação entre clientes.
     */
    @NotNull(message = "Intervalo padrão entre clientes é obrigatório")
    @Positive(message = "Intervalo padrão entre clientes deve ser positivo")
    private Integer defaultBreakMinutes;

    public BarberScheduleRequest() {
    }

    public BarberScheduleRequest(Integer maxBookingDays, Integer defaultBreakMinutes) {
        this.maxBookingDays = maxBookingDays;
        this.defaultBreakMinutes = defaultBreakMinutes;
    }

    public Integer getMaxBookingDays() {
        return maxBookingDays;
    }

    public void setMaxBookingDays(Integer maxBookingDays) {
        this.maxBookingDays = maxBookingDays;
    }

    public Integer getDefaultBreakMinutes() {return defaultBreakMinutes;
    }

    public void setDefaultBreakMinutes(Integer defaultBreakMinutes) {
        this.defaultBreakMinutes = defaultBreakMinutes;
    }
}