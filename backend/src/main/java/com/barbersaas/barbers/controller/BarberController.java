package com.barbersaas.barbers.controller;

import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.dto.UpdateBarberRequest;
import com.barbersaas.barbers.service.BarberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers")
public class BarberController {

    private final BarberService barberService;

    public BarberController(BarberService barberService) {
        this.barberService = barberService;
    }

    @PostMapping
    public ResponseEntity<BarberResponse> create(@Valid @RequestBody CreateBarberRequest request) {
        BarberResponse response = barberService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BarberResponse>> findAll() {
        List<BarberResponse> responses = barberService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponse> findById(@PathVariable UUID id) {
        BarberResponse response = barberService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBarberRequest request) {
        BarberResponse response = barberService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        barberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}