package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreatePublicAppointmentRequest;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.appointments.service.PublicAppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/appointments")
public class PublicAppointmentController {

    private final PublicAppointmentService publicAppointmentService;
    private final AppointmentService appointmentService;

    public PublicAppointmentController(
            PublicAppointmentService publicAppointmentService,
            AppointmentService appointmentService) {
        this.publicAppointmentService = publicAppointmentService;
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicAppointmentResponse createAppointment(
            @RequestParam UUID barberId,
            @Valid @RequestBody CreatePublicAppointmentRequest request) {
        
        return publicAppointmentService.createPublicAppointment(barberId, request);
    }

    @DeleteMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelAppointment(@PathVariable UUID token) {
        appointmentService.cancelByToken(token);
    }
}
