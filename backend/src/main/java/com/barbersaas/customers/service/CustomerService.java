package com.barbersaas.customers.service;

import com.barbersaas.customers.dto.CreateCustomerRequest;
import com.barbersaas.customers.dto.CustomerSummaryResponse;
import com.barbersaas.customers.dto.UpdateCustomerRequest;
import com.barbersaas.customers.dto.CustomerResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.mapper.CustomerMapper;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final BarberRepository barberRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            BarberRepository barberRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.barberRepository = barberRepository;
    }

    public CustomerResponse create(UUID barberId, CreateCustomerRequest request) {
        CustomerEntity entity = customerMapper.toEntity(request);
        entity.setBarbershop(barber(barberId).getBarbershop());
        CustomerEntity savedEntity = customerRepository.save(entity);
        return customerMapper.toResponse(savedEntity);
    }

    public List<CustomerResponse> findAll(UUID barberId) {
        return customerRepository.findDistinctByBarberId(barber(barberId).getBarbershop().getId())
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> findSummariesByBarber(
            UUID barberId,
            String query) {

        if (!barberRepository.existsById(barberId)) {
            throw new NotFoundException("Barbeiro não encontrado.");
        }

        BarberEntity barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));

        String normalizedQuery =
                query == null || query.trim().isEmpty()
                        ? null
                        : query.trim();

        List<CustomerEntity> customers = normalizedQuery == null
                ? customerRepository.findDistinctByBarberId(barber.getBarbershop().getId())
                : customerRepository.findDistinctByBarberIdAndQuery(
                        barber.getBarbershop().getId(),
                        "%" + normalizedQuery + "%"
                );

        return customers
                .stream()
                .sorted(Comparator.comparing(
                        CustomerEntity::getName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(customer -> new CustomerSummaryResponse(
                        customer.getId(),
                        customer.getName(),
                        customer.getPhone()
                ))
                .collect(Collectors.toList());
    }

    public CustomerResponse findById(UUID barberId, UUID id) {
        return customerMapper.toResponse(customerForBarber(barberId, id));
    }

    public CustomerResponse update(UUID barberId, UUID id, UpdateCustomerRequest request) {
        CustomerEntity entity = customerForBarber(barberId, id);
        
        customerMapper.updateEntity(entity, request);
        CustomerEntity updatedEntity = customerRepository.save(entity);
        
        return customerMapper.toResponse(updatedEntity);
    }

    public void delete(UUID barberId, UUID id) {
        CustomerEntity entity = customerForBarber(barberId, id);
        entity.setActive(false);
        customerRepository.save(entity);
    }

    private BarberEntity barber(UUID barberId) {
        if (!barberRepository.existsById(barberId)) {
            throw new NotFoundException("Barbeiro não encontrado.");
        }
        return barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado."));
    }

    private CustomerEntity customerForBarber(UUID barberId, UUID customerId) {
        UUID shopId = barber(barberId).getBarbershop().getId();
        return customerRepository.findById(customerId)
                .filter(customer -> shopId.equals(customer.getBarbershop().getId()))
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
    }
}
