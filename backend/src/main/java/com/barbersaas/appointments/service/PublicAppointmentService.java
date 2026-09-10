package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.CreatePublicAppointmentRequest;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class PublicAppointmentService {

    private final AppointmentService appointmentService;
    private final CustomerRepository customerRepository;
    private final BarberRepository barberRepository;

    public PublicAppointmentService(
            AppointmentService appointmentService,
            CustomerRepository customerRepository, BarberRepository barberRepository) {
        this.appointmentService = appointmentService;
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
    }

    public PublicAppointmentResponse createPublicAppointment(UUID barberId, CreatePublicAppointmentRequest request) {
        // Localizar ou criar o cliente pelo telefone
        BarberEntity barber = findBarber(barberId);
        CustomerEntity customer = findOrCreateCustomer(barber, request.getCustomerName(), request.getCustomerPhone());

        // Converter para CreateAppointmentRequest e reutilizar o AppointmentService
        return appointmentService.createPublic(
                barberId,
                convertToAppointmentRequest(request, customer.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<PublicAppointmentResponse> findPublicAppointments(UUID barberId, String customerPhone) {
        return customerRepository.findByBarbershopIdAndPhone(findBarber(barberId).getBarbershop().getId(), customerPhone)
                .map(customer -> appointmentService.findPublicByCustomerId(customer.getId()))
                .orElseGet(List::of);
    }

    private BarberEntity findBarber(UUID barberId) {
        return barberRepository.findById(barberId).orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));
    }

    private CustomerEntity findOrCreateCustomer(BarberEntity barber, String customerName, String customerPhone) {
        // Primeiro tenta encontrar pelo telefone
        return customerRepository.findByBarbershopIdAndPhone(barber.getBarbershop().getId(), customerPhone)
                .orElseGet(() -> {
                    // Se não existir, cria um novo cliente
                    CustomerEntity newCustomer = new CustomerEntity();
                    newCustomer.setName(customerName);
                    newCustomer.setPhone(customerPhone);
                    newCustomer.setActive(true);
                    newCustomer.setBarbershop(barber.getBarbershop());
                    return customerRepository.save(newCustomer);
                });
    }

    private com.barbersaas.appointments.dto.CreateAppointmentRequest convertToAppointmentRequest(
            CreatePublicAppointmentRequest publicRequest, UUID customerId) {
        
        com.barbersaas.appointments.dto.CreateAppointmentRequest request = 
            new com.barbersaas.appointments.dto.CreateAppointmentRequest();
        
        request.setCustomerId(customerId);
        request.setServiceIds(publicRequest.getServiceIds());
        request.setAppointmentDateTime(publicRequest.getAppointmentDateTime());
        request.setNotes(publicRequest.getNotes());
        
        return request;
    }
}
