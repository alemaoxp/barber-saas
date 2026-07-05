package com.barbersaas.barberschedules.mapper;

import com.barbersaas.barberschedules.dto.BarberScheduleRequest;
import com.barbersaas.barberschedules.dto.BarberScheduleResponse;
import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class BarberScheduleMapper {

    /**
     * Converte uma entidade BarberScheduleEntity para um DTO BarberScheduleResponse.
     * @param entity Entidade a ser convertida
     * @return DTO de resposta com os dados da entidade
     */
    public BarberScheduleResponse toResponse(BarberScheduleEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BarberScheduleResponse(
                entity.getId(),
                entity.getBarber() != null ? entity.getBarber().getId() : null,
                entity.getMaxBookingDays(),
                entity.getDefaultBreakMinutes()
        );
    }

    /**
     * Atualiza uma entidade BarberScheduleEntity com os dados de um DTO BarberScheduleRequest.
     * Apenas os campos atualizáveis são modificados.
     * @param entity Entidade a ser atualizada
     * @param request DTO com os novos dados
     */
    public void updateEntity(BarberScheduleEntity entity, BarberScheduleRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setMaxBookingDays(request.getMaxBookingDays());
        entity.setDefaultBreakMinutes(request.getDefaultBreakMinutes());
    }
}