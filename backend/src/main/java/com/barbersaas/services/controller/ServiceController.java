package com.barbersaas.services.controller;

import com.barbersaas.services.dto.CreateServiceRequest;
import com.barbersaas.services.dto.ServiceResponse;
import com.barbersaas.services.dto.UpdateServiceRequest;
import com.barbersaas.services.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@PathVariable UUID barberId, @Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.create(barberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> findAll(@PathVariable UUID barberId) {
        List<ServiceResponse> responses = serviceService.findAll(barberId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> findById(@PathVariable UUID barberId, @PathVariable UUID id) {
        ServiceResponse response = serviceService.findById(barberId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable UUID barberId, @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request) {
        ServiceResponse response = serviceService.update(barberId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID barberId, @PathVariable UUID id) {
        serviceService.delete(barberId, id);
        return ResponseEntity.noContent().build();
    }
}
