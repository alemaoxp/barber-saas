package com.barbersaas.services.controller;

import com.barbersaas.services.dto.ServiceResponse;
import com.barbersaas.services.service.ServiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/public/barbers/{barberId}/services")
public class PublicServiceController {

    private final ServiceService serviceService;

    public PublicServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceResponse> findActive(@PathVariable UUID barberId) {
        return serviceService.findActive(barberId);
    }
}
