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
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.availabilityinterest.mapper.AvailabilityInterestMapper;
import com.barbersaas.availabilityinterest.repository.AvailabilityInterestRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.services.entity.ServiceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityInterestServiceTest {

    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID INTEREST_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SLOT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPOINTMENT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID OTHER_BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000007");

    @Mock
    private AvailabilityInterestRepository availabilityInterestRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailableSlotRepository availableSlotRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AppointmentService appointmentService;

    private AvailabilityInterestService availabilityInterestService;
    private CustomerEntity customer;
    private BarberEntity barber;
    private ServiceEntity service;
    private AppointmentEntity appointment;
    private AvailableSlotEntity slot;
    private AvailabilityInterestEntity interest;

    @BeforeEach
    void setUp() {
        availabilityInterestService =
                new AvailabilityInterestService(
                        availabilityInterestRepository,
                        appointmentRepository,
                        availableSlotRepository,
                        customerRepository,
                        new AvailabilityInterestMapper(),
                        appointmentService
                );

        customer = customer();
        barber = barber();
        service = service();
        appointment = appointment(LocalDateTime.now().plusDays(3));
        slot = slot(LocalDateTime.now().plusDays(1));
        interest = new AvailabilityInterestEntity(customer, appointment);
        ReflectionTestUtils.setField(interest, "id", INTEREST_ID);
    }

    @Test
    void acceptShouldMoveAppointmentAndCompleteInterest() {
        when(availabilityInterestRepository.findByIdForUpdate(INTEREST_ID))
                .thenReturn(Optional.of(interest));
        when(availableSlotRepository.findByIdForUpdate(SLOT_ID))
                .thenReturn(Optional.of(slot));
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment));
        when(availabilityInterestRepository.save(interest))
                .thenReturn(interest);

        AvailabilityInterestResponse response =
                availabilityInterestService.accept(
                        CUSTOMER_ID,
                        INTEREST_ID,
                        new AcceptAvailabilityInterestRequest(SLOT_ID)
                );

        assertEquals(slot.getAvailableDateTime(),
                appointment.getAppointmentDateTime());
        assertEquals(AvailableSlotStatus.BOOKED, slot.getStatus());
        assertEquals(AvailabilityInterestStatus.COMPLETED,
                interest.getStatus());
        assertEquals(AvailabilityInterestStatus.COMPLETED,
                response.getStatus());

        verify(appointmentService)
                .validateAvailabilityIgnoringAppointment(
                        BARBER_ID,
                        slot.getAvailableDateTime(),
                        APPOINTMENT_ID
                );
        verify(availableSlotRepository).save(slot);
        verify(appointmentRepository).save(appointment);
        verify(availabilityInterestRepository).save(interest);
    }

    @Test
    void acceptShouldRejectSlotThatDoesNotAnticipateAppointment() {
        slot.setAvailableDateTime(
                appointment.getAppointmentDateTime().plusMinutes(30)
        );

        when(availabilityInterestRepository.findByIdForUpdate(INTEREST_ID))
                .thenReturn(Optional.of(interest));
        when(availableSlotRepository.findByIdForUpdate(SLOT_ID))
                .thenReturn(Optional.of(slot));
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> availabilityInterestService.accept(
                                CUSTOMER_ID,
                                INTEREST_ID,
                                new AcceptAvailabilityInterestRequest(SLOT_ID)
                        )
                );

        assertEquals("Vaga não antecipa o agendamento.",
                exception.getMessage());
    }

    @Test
    void acceptShouldRejectUnavailableSlot() {
        slot.setStatus(AvailableSlotStatus.BOOKED);

        when(availabilityInterestRepository.findByIdForUpdate(INTEREST_ID))
                .thenReturn(Optional.of(interest));
        when(availableSlotRepository.findByIdForUpdate(SLOT_ID))
                .thenReturn(Optional.of(slot));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> availabilityInterestService.accept(
                                CUSTOMER_ID,
                                INTEREST_ID,
                                new AcceptAvailabilityInterestRequest(SLOT_ID)
                        )
                );

        assertEquals("Vaga de antecipação indisponível.",
                exception.getMessage());
    }

    @Test
    void activeInterestShouldReturnEligibleOpportunity() {
        when(availabilityInterestRepository.findById(INTEREST_ID))
                .thenReturn(Optional.of(interest));
        when(availableSlotRepository
                .findByBarberIdAndStatusAndAvailableDateTimeBetweenOrderByAvailableDateTimeAsc(
                        eq(BARBER_ID),
                        eq(AvailableSlotStatus.AVAILABLE),
                        any(),
                        eq(appointment.getAppointmentDateTime())
                )).thenReturn(List.of(slot));

        List<AvailabilityOpportunityResponse> opportunities =
                availabilityInterestService.findOpportunities(
                        CUSTOMER_ID,
                        INTEREST_ID
                );

        assertEquals(1, opportunities.size());
        assertEquals(SLOT_ID,
                opportunities.get(0).getAvailableSlotId());
        assertEquals(slot.getAvailableDateTime(),
                opportunities.get(0).getAvailableDateTime());
        verify(appointmentService)
                .validateAvailabilityIgnoringAppointment(
                        BARBER_ID,
                        slot.getAvailableDateTime(),
                        APPOINTMENT_ID
                );
    }

    @Test
    void opportunityAfterAppointmentShouldNotAppear() {
        slot.setAvailableDateTime(
                appointment.getAppointmentDateTime().plusMinutes(30)
        );

        assertNoOpportunities(List.of(slot));
    }

    @Test
    void pastOpportunityShouldNotAppear() {
        slot.setAvailableDateTime(LocalDateTime.now().minusDays(1));

        assertNoOpportunities(List.of(slot));
    }

    @Test
    void opportunityFromAnotherBarberShouldNotAppear() {
        slot.setBarber(otherBarber());

        assertNoOpportunities(List.of(slot));
    }

    @Test
    void unavailableOpportunityShouldNotAppear() {
        slot.setStatus(AvailableSlotStatus.BOOKED);

        assertNoOpportunities(List.of(slot));
    }

    @Test
    void customerShouldNotSeeOpportunityFromAnotherCustomersInterest() {
        when(availabilityInterestRepository.findById(INTEREST_ID))
                .thenReturn(Optional.of(interest));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> availabilityInterestService.findOpportunities(
                                OTHER_CUSTOMER_ID,
                                INTEREST_ID
                        )
                );

        assertEquals("A solicitação não pertence ao cliente.",
                exception.getMessage());
    }

    @Test
    void inactiveInterestShouldNotReturnOpportunities() {
        interest.setStatus(AvailabilityInterestStatus.CANCELED);
        when(availabilityInterestRepository.findById(INTEREST_ID))
                .thenReturn(Optional.of(interest));

        List<AvailabilityOpportunityResponse> opportunities =
                availabilityInterestService.findOpportunities(
                        CUSTOMER_ID,
                        INTEREST_ID
                );

        assertTrue(opportunities.isEmpty());
        verify(availableSlotRepository, never())
                .findByBarberIdAndStatusAndAvailableDateTimeBetweenOrderByAvailableDateTimeAsc(
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void findActiveShouldReturnTheCustomersActiveInterestForAppointment() {
        when(availabilityInterestRepository
                .findByCustomerIdAndAppointmentIdAndStatus(
                        CUSTOMER_ID,
                        APPOINTMENT_ID,
                        AvailabilityInterestStatus.ACTIVE
                )).thenReturn(Optional.of(interest));

        Optional<AvailabilityInterestResponse> response =
                availabilityInterestService.findActive(
                        CUSTOMER_ID,
                        APPOINTMENT_ID
                );

        assertTrue(response.isPresent());
        assertEquals(INTEREST_ID, response.get().getId());
        assertEquals(AvailabilityInterestStatus.ACTIVE,
                response.get().getStatus());
    }

    private CustomerEntity customer() {
        CustomerEntity entity = new CustomerEntity(
                "Cliente",
                "(11) 98888-8888",
                "cliente@example.com",
                null,
                null,
                true
        );
        ReflectionTestUtils.setField(entity, "id", CUSTOMER_ID);
        return entity;
    }

    private BarberEntity barber() {
        BarberEntity entity = new BarberEntity(
                "Barbeiro",
                "barbeiro@example.com",
                "(11) 99999-9999",
                "Corte",
                true
        );
        ReflectionTestUtils.setField(entity, "id", BARBER_ID);
        return entity;
    }

    private BarberEntity otherBarber() {
        BarberEntity entity = new BarberEntity(
                "Outro Barbeiro",
                "outro@example.com",
                "(11) 97777-7777",
                "Corte",
                true
        );
        ReflectionTestUtils.setField(entity, "id", OTHER_BARBER_ID);
        return entity;
    }

    private ServiceEntity service() {
        ServiceEntity entity = new ServiceEntity(
                "Corte",
                "Corte masculino",
                40,
                BigDecimal.valueOf(50),
                true
        );
        return entity;
    }

    private AppointmentEntity appointment(LocalDateTime dateTime) {
        AppointmentEntity entity = new AppointmentEntity(
                customer,
                barber,
                List.of(service),
                BigDecimal.valueOf(50),
                dateTime,
                AppointmentStatus.SCHEDULED,
                "teste"
        );
        ReflectionTestUtils.setField(entity, "id", APPOINTMENT_ID);
        return entity;
    }

    private AvailableSlotEntity slot(LocalDateTime dateTime) {
        AvailableSlotEntity entity =
                new AvailableSlotEntity(barber, dateTime);
        ReflectionTestUtils.setField(entity, "id", SLOT_ID);
        return entity;
    }

    private void assertNoOpportunities(List<AvailableSlotEntity> slots) {
        when(availabilityInterestRepository.findById(INTEREST_ID))
                .thenReturn(Optional.of(interest));
        when(availableSlotRepository
                .findByBarberIdAndStatusAndAvailableDateTimeBetweenOrderByAvailableDateTimeAsc(
                        eq(BARBER_ID),
                        eq(AvailableSlotStatus.AVAILABLE),
                        any(),
                        eq(appointment.getAppointmentDateTime())
                )).thenReturn(slots);

        List<AvailabilityOpportunityResponse> opportunities =
                availabilityInterestService.findOpportunities(
                        CUSTOMER_ID,
                        INTEREST_ID
                );

        assertTrue(opportunities.isEmpty());
    }
}
