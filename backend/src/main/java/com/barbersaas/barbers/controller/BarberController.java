package com.barbersaas.barbers.controller;

import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.dto.UpdateBarberRequest;
import com.barbersaas.barbers.service.BarberService;
import com.barbersaas.auth.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<BarberResponse> create(
            @Valid @RequestBody CreateBarberRequest request,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        BarberResponse response = barberService.create(adminPrincipal.barbershopId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BarberResponse>> findAll(
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        List<BarberResponse> responses = barberService.findAll(adminPrincipal.barbershopId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        BarberResponse response = barberService.findById(adminPrincipal.barbershopId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBarberRequest request,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        BarberResponse response = barberService.update(adminPrincipal.barbershopId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        barberService.delete(adminPrincipal.barbershopId(), id);
        return ResponseEntity.noContent().build();
    }
}
