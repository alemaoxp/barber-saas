package com.barbersaas.weeklyschedule.controller;

import com.barbersaas.weeklyschedule.dto.WeeklyScheduleRequest;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleResponse;
import com.barbersaas.weeklyschedule.service.WeeklyScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/weekly-schedule")
public class WeeklyScheduleController {

    private final WeeklyScheduleService weeklyScheduleService;

    public WeeklyScheduleController(WeeklyScheduleService weeklyScheduleService) {
        this.weeklyScheduleService = weeklyScheduleService;
    }

    @GetMapping
    public ResponseEntity<WeeklyScheduleResponse> getWeeklySchedule(
            @PathVariable UUID barberId) {
        return ResponseEntity.ok(
                weeklyScheduleService.getWeeklySchedule(barberId)
        );
    }

    @PutMapping
    public ResponseEntity<WeeklyScheduleResponse> updateWeeklySchedule(
            @PathVariable UUID barberId,
            @RequestBody WeeklyScheduleRequest request) {
        return ResponseEntity.ok(
                weeklyScheduleService.updateWeeklySchedule(barberId, request)
        );
    }
}