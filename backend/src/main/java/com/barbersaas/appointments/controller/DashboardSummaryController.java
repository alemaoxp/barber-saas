package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.DashboardSummaryResponse;
import com.barbersaas.appointments.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/dashboard")
public class DashboardSummaryController {
    private final AppointmentService appointmentService;

    public DashboardSummaryController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @PathVariable UUID barberId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(
                appointmentService.getDashboardSummary(barberId, startDate, endDate)
        );
    }
}
