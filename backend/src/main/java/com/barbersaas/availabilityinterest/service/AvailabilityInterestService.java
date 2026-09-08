package com.barbersaas.availabilityinterest.service;

import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.availableslot.entity.AvailableSlotEntity;
import com.barbersaas.availableslot.enums.AvailableSlotStatus;
import com.barbersaas.availableslot.repository.AvailableSlotRepository;
import com.barbersaas.availabilityinterest.dto.AcceptAvailabilityInterestRequest;
import com.barbersaas.availabilityinterest.dto.AvailabilityInterestResponse;
import com.barbersaas.availabilityinterest.dto.AvailabilityOpportunityResponse;
import com.barbersaas.availabilityinterest.dto.CreateAvailabilityInterestRequest;
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.availabilityinterest.mapper.AvailabilityInterestMapper;
import com.barbersaas.availabilityinterest.repository.AvailabilityInterestRepository;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AvailabilityInterestService {

    private final AvailabilityInterestRepository availabilityInterestRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailableSlotRepository availableSlotRepository;
    private final CustomerRepository customerRepository;
    private final AvailabilityInterestMapper availabilityInterestMapper;
    private final AppointmentService appointmentService;

    public AvailabilityInterestService(
            AvailabilityInterestRepository availabilityInterestRepository,
            AppointmentRepository appointmentRepository,
            AvailableSlotRepository availableSlotRepository,
            CustomerRepository customerRepository,
            AvailabilityInterestMapper availabilityInterestMapper,
            AppointmentService appointmentService) {

        this.availabilityInterestRepository =
                availabilityInterestRepository;

        this.appointmentRepository =
                appointmentRepository;

        this.availableSlotRepository =
                availableSlotRepository;

        this.customerRepository =
                customerRepository;

        this.availabilityInterestMapper =
                availabilityInterestMapper;

        this.appointmentService =
                appointmentService;
    }

    public AvailabilityInterestResponse create(
            UUID customerId,
            CreateAvailabilityInterestRequest request) {

        CustomerEntity customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Cliente não encontrado."
                                )
                        );

        AppointmentEntity appointment =
                appointmentRepository.findById(
                                request.getAppointmentId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Agendamento não encontrado."
                                )
                        );

        validateAppointment(customer, appointment);

        boolean alreadyExists =
                availabilityInterestRepository
                        .existsByCustomerIdAndAppointmentIdAndStatus(
                                customerId,
                                appointment.getId(),
                                AvailabilityInterestStatus.ACTIVE
                        );

        if (alreadyExists) {
            throw new BusinessException(
                    "Cliente já está aguardando uma vaga para este agendamento."
            );
        }

        AvailabilityInterestEntity entity =
                new AvailabilityInterestEntity(
                        customer,
                        appointment
                );

        AvailabilityInterestEntity savedEntity =
                availabilityInterestRepository.save(entity);

        return availabilityInterestMapper.toResponse(
                savedEntity
        );
    }

    public AvailabilityInterestResponse findById(
            UUID interestId) {

        AvailabilityInterestEntity entity =
                availabilityInterestRepository.findById(
                                interestId
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Solicitação de antecipação não encontrada."
                                )
                        );

        return availabilityInterestMapper.toResponse(
                entity
        );
    }

    public java.util.Optional<AvailabilityInterestResponse> findActive(
            UUID customerId,
            UUID appointmentId) {

        return availabilityInterestRepository
                .findByCustomerIdAndAppointmentIdAndStatus(
                        customerId,
                        appointmentId,
                        AvailabilityInterestStatus.ACTIVE
                )
                .map(availabilityInterestMapper::toResponse);
    }

    public List<AvailabilityOpportunityResponse> findOpportunities(
            UUID customerId,
            UUID interestId) {

        AvailabilityInterestEntity interest =
                availabilityInterestRepository.findById(
                                interestId
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Solicitação de antecipação não encontrada."
                                )
                        );

        if (!interest.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(
                    "A solicitação não pertence ao cliente."
            );
        }

        if (interest.getStatus() !=
                AvailabilityInterestStatus.ACTIVE) {

            return List.of();
        }

        AppointmentEntity appointment =
                interest.getAppointment();

        LocalDateTime now =
                LocalDateTime.now();

        if (!appointment.getAppointmentDateTime().isAfter(now)) {
            return List.of();
        }

        UUID barberId =
                appointment.getBarber().getId();

        return availableSlotRepository
                .findByBarberIdAndStatusAndAvailableDateTimeBetweenOrderByAvailableDateTimeAsc(
                        barberId,
                        AvailableSlotStatus.AVAILABLE,
                        now,
                        appointment.getAppointmentDateTime()
                )
                .stream()
                .filter(slot ->
                        isEligibleSlot(
                                appointment,
                                slot,
                                now
                        )
                )
                .filter(slot ->
                        isAvailableForAppointment(
                                appointment,
                                slot
                        )
                )
                .map(slot ->
                        new AvailabilityOpportunityResponse(
                                slot.getId(),
                                slot.getAvailableDateTime()
                        )
                )
                .collect(Collectors.toList());
    }

    public void cancel(
            UUID customerId,
            UUID interestId) {

        AvailabilityInterestEntity entity =
                availabilityInterestRepository.findById(
                                interestId
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Solicitação de antecipação não encontrada."
                                )
                        );

        if (!entity.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(
                    "A solicitação não pertence ao cliente."
            );
        }

        if (entity.getStatus() !=
                AvailabilityInterestStatus.ACTIVE) {

            throw new BusinessException(
                    "A solicitação não está ativa."
            );
        }

        entity.setStatus(
                AvailabilityInterestStatus.CANCELED
        );

        availabilityInterestRepository.save(entity);
    }

    @Transactional
    public AvailabilityInterestResponse accept(
            UUID customerId,
            UUID interestId,
            AcceptAvailabilityInterestRequest request) {

        AvailabilityInterestEntity interest =
                availabilityInterestRepository.findByIdForUpdate(
                                interestId
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Solicitação de antecipação não encontrada."
                                )
                        );

        if (!interest.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(
                    "A solicitação não pertence ao cliente."
            );
        }

        if (interest.getStatus() !=
                AvailabilityInterestStatus.ACTIVE) {

            throw new BusinessException(
                    "A solicitação não está ativa."
            );
        }

        AvailableSlotEntity slot =
                availableSlotRepository.findByIdForUpdate(
                                request.getAvailableSlotId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Vaga de antecipação não encontrada."
                                )
                        );

        if (slot.getStatus() != AvailableSlotStatus.AVAILABLE) {
            throw new BusinessException(
                    "Vaga de antecipação indisponível."
            );
        }

        AppointmentEntity appointment =
                appointmentRepository.findByIdForUpdate(
                                interest.getAppointment().getId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Agendamento não encontrado."
                                )
                        );

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException(
                    "Agendamento não está ativo."
            );
        }

        UUID barberId =
                appointment.getBarber().getId();

        if (!slot.getBarber().getId().equals(barberId)) {
            throw new BusinessException(
                    "Vaga não pertence ao mesmo barbeiro do agendamento."
            );
        }

        LocalDateTime slotDateTime =
                slot.getAvailableDateTime();

        if (!slotDateTime.isBefore(
                appointment.getAppointmentDateTime())) {

            throw new BusinessException(
                    "Vaga não antecipa o agendamento."
            );
        }

        if (!slotDateTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    "Vaga de antecipação não está mais no futuro."
            );
        }

        appointmentService.validateAvailabilityIgnoringAppointment(
                barberId,
                slotDateTime,
                appointment.getId()
        );

        slot.setStatus(AvailableSlotStatus.RESERVED);
        appointment.setAppointmentDateTime(slotDateTime);
        slot.setStatus(AvailableSlotStatus.BOOKED);
        interest.setStatus(AvailabilityInterestStatus.COMPLETED);

        availableSlotRepository.save(slot);
        appointmentRepository.save(appointment);
        AvailabilityInterestEntity savedInterest =
                availabilityInterestRepository.save(interest);

        return availabilityInterestMapper.toResponse(savedInterest);
    }

    private void validateAppointment(
            CustomerEntity customer,
            AppointmentEntity appointment) {

        if (!appointment.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new BusinessException(
                    "O agendamento não pertence ao cliente."
            );
        }

        if (appointment.getStatus() !=
                AppointmentStatus.SCHEDULED) {

            throw new BusinessException(
                    "Somente agendamentos ativos podem entrar na fila de antecipação."
            );
        }

        if (!appointment.getAppointmentDateTime()
                .isAfter(LocalDateTime.now())) {

            throw new BusinessException(
                    "Somente agendamentos futuros podem entrar na fila de antecipação."
            );
        }
    }

    private boolean isAvailableForAppointment(
            AppointmentEntity appointment,
            AvailableSlotEntity slot) {

        try {
            appointmentService.validateAvailabilityIgnoringAppointment(
                    appointment.getBarber().getId(),
                    slot.getAvailableDateTime(),
                    appointment.getId()
            );
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    private boolean isEligibleSlot(
            AppointmentEntity appointment,
            AvailableSlotEntity slot,
            LocalDateTime now) {

        LocalDateTime slotDateTime =
                slot.getAvailableDateTime();

        return slot.getStatus() == AvailableSlotStatus.AVAILABLE
                && slot.getBarber().getId()
                .equals(appointment.getBarber().getId())
                && slotDateTime.isAfter(now)
                && slotDateTime.isBefore(
                appointment.getAppointmentDateTime()
        );
    }
}
