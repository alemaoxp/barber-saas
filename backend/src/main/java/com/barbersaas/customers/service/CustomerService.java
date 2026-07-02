package com.barbersaas.customers.service;

import com.barbersaas.customers.dto.CreateCustomerRequest;
import com.barbersaas.customers.dto.UpdateCustomerRequest;
import com.barbersaas.customers.dto.CustomerResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.mapper.CustomerMapper;
import com.barbersaas.customers.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerResponse create(CreateCustomerRequest request) {
        CustomerEntity entity = customerMapper.toEntity(request);
        CustomerEntity savedEntity = customerRepository.save(entity);
        return customerMapper.toResponse(savedEntity);
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponse findById(UUID id) {
        return customerRepository.findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
    }

    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        
        customerMapper.updateEntity(entity, request);
        CustomerEntity updatedEntity = customerRepository.save(entity);
        
        return customerMapper.toResponse(updatedEntity);
    }

    public void delete(UUID id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        
        customerRepository.delete(entity);
    }
}