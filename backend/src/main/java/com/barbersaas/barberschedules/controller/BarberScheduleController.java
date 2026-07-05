package com.barbersaas.barberschedules.controller;

import com.barbersaas.barberschedules.dto.BarberScheduleRequest;
import com.barbersaas.barberschedules.dto.BarberScheduleResponse;
import com.barbersaas.barberschedules.service.BarberScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/schedule")
public class BarberScheduleController {

    private final BarberScheduleService barberScheduleService;

    public BarberScheduleController(BarberScheduleService barberScheduleService) {
        this.barberScheduleService = barberScheduleService;
    }

    /**
     * Obtém a configuração de agenda de um barbeiro.
     * @param barberId ID do barbeiro
     * @return Configuração da agenda do barbeiro
     */
    @GetMapping
    public ResponseEntity<BarberScheduleResponse> getBarberSchedule(
            @PathVariable UUID barberId) {
        BarberScheduleResponse response = barberScheduleService.getByBarberId(barberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza a configuração de agenda de um barbeiro.
     * @param barberId ID do barbeiro
     * @param request DTO com os novos dados da configuração
     * @return Configuração atualizada
     */
    @PutMapping
    public ResponseEntity<BarberScheduleResponse> updateBarberSchedule(
            @PathVariable UUID barberId,
            @Valid @RequestBody BarberScheduleRequest request) {
        BarberScheduleResponse response = barberScheduleService.update(barberId, request);
        return ResponseEntity.ok(response);
    }
}