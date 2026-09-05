package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.CreatePublicAppointmentRequest;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PublicAppointmentService {

    private final AppointmentService appointmentService;
    private final CustomerRepository customerRepository;

    public PublicAppointmentService(
            AppointmentService appointmentService,
            CustomerRepository customerRepository) {
        this.appointmentService = appointmentService;
        this.customerRepository = customerRepository;
    }

    public PublicAppointmentResponse createPublicAppointment(UUID barberId, CreatePublicAppointmentRequest request) {
        // Localizar ou criar o cliente pelo telefone
        CustomerEntity customer = findOrCreateCustomer(request.getCustomerName(), request.getCustomerPhone());

        // Converter para CreateAppointmentRequest e reutilizar o AppointmentService
        return appointmentService.createPublic(
                barberId,
                convertToAppointmentRequest(request, customer.getId())
        );
    }

    private CustomerEntity findOrCreateCustomer(String customerName, String customerPhone) {
        // Primeiro tenta encontrar pelo telefone
        return customerRepository.findByPhone(customerPhone)
                .orElseGet(() -> {
                    // Se não existir, cria um novo cliente
                    CustomerEntity newCustomer = new CustomerEntity();
                    newCustomer.setName(customerName);
                    newCustomer.setPhone(customerPhone);
                    return customerRepository.save(newCustomer);
                });
    }

    private com.barbersaas.appointments.dto.CreateAppointmentRequest convertToAppointmentRequest(
            CreatePublicAppointmentRequest publicRequest, UUID customerId) {
        
        com.barbersaas.appointments.dto.CreateAppointmentRequest request = 
            new com.barbersaas.appointments.dto.CreateAppointmentRequest();
        
        request.setCustomerId(customerId);
        request.setServiceId(publicRequest.getServiceId());
        request.setAppointmentDateTime(publicRequest.getAppointmentDateTime());
        request.setNotes(publicRequest.getNotes());
        
        return request;
    }
}
