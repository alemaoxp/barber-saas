package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
public class PublicAvailabilityController {

    private final AppointmentService appointmentService;

    public PublicAvailabilityController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/barbers/{barberId}/available-slots")
    public List<java.time.LocalTime> getAvailableSlots(
            @PathVariable UUID barberId,
            @RequestParam LocalDate date) {
        
        return appointmentService.getAvailableSlots(barberId, date);
    }
}