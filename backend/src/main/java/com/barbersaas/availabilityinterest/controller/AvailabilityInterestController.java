package com.barbersaas.availabilityinterest.controller;

import com.barbersaas.availabilityinterest.dto.AcceptAvailabilityInterestRequest;
import com.barbersaas.availabilityinterest.dto.AvailabilityInterestResponse;
import com.barbersaas.availabilityinterest.dto.AvailabilityOpportunityResponse;
import com.barbersaas.availabilityinterest.dto.CreateAvailabilityInterestRequest;
import com.barbersaas.availabilityinterest.service.AvailabilityInterestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/availability-interests")
public class AvailabilityInterestController {

    private final AvailabilityInterestService availabilityInterestService;

    public AvailabilityInterestController(
            AvailabilityInterestService availabilityInterestService) {
        this.availabilityInterestService = availabilityInterestService;
    }

    @PostMapping
    public ResponseEntity<AvailabilityInterestResponse> create(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAvailabilityInterestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityInterestService.create(customerId, request));
    }

    @GetMapping("/{interestId}")
    public ResponseEntity<AvailabilityInterestResponse> findById(
            @PathVariable UUID customerId,
            @PathVariable UUID interestId) {
        return ResponseEntity.ok(availabilityInterestService.findById(interestId));
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID customerId,
            @PathVariable UUID interestId) {
        availabilityInterestService.cancel(customerId, interestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{interestId}/accept")
    public ResponseEntity<AvailabilityInterestResponse> accept(
            @PathVariable UUID customerId,
            @PathVariable UUID interestId,
            @Valid @RequestBody AcceptAvailabilityInterestRequest request) {
        return ResponseEntity.ok(
                availabilityInterestService.accept(customerId, interestId, request));
    }

    @GetMapping("/{interestId}/opportunities")
    public ResponseEntity<List<AvailabilityOpportunityResponse>> findOpportunities(
            @PathVariable UUID customerId,
            @PathVariable UUID interestId) {
        return ResponseEntity.ok(
                availabilityInterestService.findOpportunities(
                        customerId,
                        interestId
                )
        );
    }
}
