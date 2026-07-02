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
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> findAll() {
        List<ServiceResponse> responses = serviceService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> findById(@PathVariable UUID id) {
        ServiceResponse response = serviceService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request) {
        ServiceResponse response = serviceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}