package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @PathVariable UUID barberId,
            @Valid @RequestBody CreateAppointmentRequest request) {

        AppointmentResponse response =
                appointmentService.create(
                        barberId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findAll(
            @PathVariable UUID barberId) {

        List<AppointmentResponse> responses =
                appointmentService.findAll(barberId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable UUID barberId,
            @RequestParam LocalDate date) {

        List<LocalTime> slots =
                appointmentService.getAvailableSlots(
                        barberId,
                        date
                );

        return ResponseEntity.ok(slots);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> findById(
            @PathVariable UUID barberId,
            @PathVariable UUID appointmentId) {

        AppointmentResponse response =
                appointmentService.findById(
                        barberId,
                        appointmentId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable UUID barberId,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request) {

        AppointmentResponse response =
                appointmentService.update(
                        barberId,
                        appointmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID barberId,
            @PathVariable UUID appointmentId) {

        appointmentService.delete(
                barberId,
                appointmentId
        );

        return ResponseEntity.noContent().build();
    }
}