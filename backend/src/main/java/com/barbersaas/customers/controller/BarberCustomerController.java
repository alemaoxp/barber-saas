package com.barbersaas.customers.controller;

import com.barbersaas.customers.dto.CustomerSummaryResponse;
import com.barbersaas.customers.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/customers")
public class BarberCustomerController {

    private final CustomerService customerService;

    public BarberCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerSummaryResponse>> findByBarber(
            @PathVariable UUID barberId,
            @RequestParam(required = false) String query) {

        return ResponseEntity.ok(
                customerService.findSummariesByBarber(
                        barberId,
                        query
                )
        );
    }
}
