package com.barbersaas.scheduleblock.controller;

import com.barbersaas.scheduleblock.dto.CreateScheduleBlockRequest;
import com.barbersaas.scheduleblock.dto.ScheduleBlockResponse;
import com.barbersaas.scheduleblock.dto.UpdateScheduleBlockRequest;
import com.barbersaas.scheduleblock.service.ScheduleBlockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/schedule-blocks")
public class ScheduleBlockController {

    private final ScheduleBlockService scheduleBlockService;

    public ScheduleBlockController(ScheduleBlockService scheduleBlockService) {
        this.scheduleBlockService = scheduleBlockService;
     }

    @PostMapping
    public ResponseEntity<ScheduleBlockResponse> create(
            @PathVariable UUID barberId,
            @Valid @RequestBody CreateScheduleBlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleBlockService.create(barberId, request));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleBlockResponse>> findAll(
            @PathVariable UUID barberId) {
        return ResponseEntity.ok(scheduleBlockService.findAll(barberId));
    }

    @GetMapping("/{scheduleBlockId}")
    public ResponseEntity<ScheduleBlockResponse> findById(
            @PathVariable UUID barberId,
            @PathVariable UUID scheduleBlockId) {
        return ResponseEntity.ok(scheduleBlockService.findById(barberId, scheduleBlockId));
    }

    @PutMapping("/{scheduleBlockId}")
    public ResponseEntity<ScheduleBlockResponse> update(
            @PathVariable UUID barberId,
            @PathVariable UUID scheduleBlockId,
            @Valid @RequestBody UpdateScheduleBlockRequest request) {
        return ResponseEntity.ok(scheduleBlockService.update(barberId, scheduleBlockId, request));
    }

    @DeleteMapping("/{scheduleBlockId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID barberId,
            @PathVariable UUID scheduleBlockId) {
        scheduleBlockService.delete(barberId, scheduleBlockId);
        return ResponseEntity.noContent().build();
    }
}
