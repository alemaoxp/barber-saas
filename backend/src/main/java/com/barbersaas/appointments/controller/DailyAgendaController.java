package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.DailyAgendaResponse;
import com.barbersaas.appointments.service.AppointmentService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}")
public class DailyAgendaController {

    private final AppointmentService appointmentService;

    public DailyAgendaController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/daily-agenda")
    public ResponseEntity<DailyAgendaResponse> getDailyAgenda(
            @PathVariable UUID barberId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                appointmentService.getDailyAgenda(
                        barberId,
                        date
                )
        );
    }

    @GetMapping("/next-appointment")
    public ResponseEntity<?> getNextScheduledAppointment(
            @PathVariable UUID barberId) {

        return appointmentService
                .findNextScheduledAppointment(barberId, LocalDateTime.now())
                .<ResponseEntity<?>>map(slot -> ResponseEntity.ok()
                        .cacheControl(CacheControl.noStore())
                        .body(slot))
                .orElseGet(() -> ResponseEntity.noContent()
                        .cacheControl(CacheControl.noStore())
                        .build());
    }
}
