package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.mapper.AppointmentMapper;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository,
            AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentMapper = appointmentMapper;
    }

    public AppointmentResponse create(CreateAppointmentRequest request) {
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        BarberEntity barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));

        // Verificar conflito de horário
        boolean exists = appointmentRepository.existsByBarberAndDateAndTime(barber, request.getDate(), request.getTime());
        if (exists) {
            throw new RuntimeException("Este horário já está ocupado para este barbeiro.");
        }

        AppointmentEntity entity = appointmentMapper.toEntity(customer, barber, service, request);
        AppointmentEntity savedEntity = appointmentRepository.save(entity);
        return appointmentMapper.toResponse(savedEntity);
    }

    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAll()
                .stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse findById(UUID id) {
        return appointmentRepository.findById(id)
                .map(appointmentMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
    }

    public AppointmentResponse update(UUID id, UpdateAppointmentRequest request) {
        AppointmentEntity entity = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        BarberEntity barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado."));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));

        // Verificar conflito de horário (excluindo o próprio agendamento que está sendo atualizado)
        boolean exists = appointmentRepository.existsByBarberAndDateAndTime(barber, request.getDate(), request.getTime());
        if (exists && !entity.getBarber().getId().equals(barber.getId())) {
            throw new RuntimeException("Este horário já está ocupado para este barbeiro.");
        }

        appointmentMapper.updateEntity(entity, customer, barber, service, request);
        AppointmentEntity updatedEntity = appointmentRepository.save(entity);
        return appointmentMapper.toResponse(updatedEntity);
    }

    public void delete(UUID id) {
        AppointmentEntity entity = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
        appointmentRepository.delete(entity);
    }
}