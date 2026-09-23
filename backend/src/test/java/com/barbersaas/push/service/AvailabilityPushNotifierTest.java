package com.barbersaas.push.service;

import com.barbersaas.appointments.event.AvailableSlotCreatedEvent;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.availabilityinterest.repository.AvailabilityInterestRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.services.entity.ServiceEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityPushNotifierTest {

    private static final UUID BARBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID INTEREST_A_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID INTEREST_B_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID AVAILABLE_SLOT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final LocalDateTime SLOT_TIME = LocalDateTime.of(2026, 9, 10, 10, 0);

    @Mock
    private AvailabilityInterestRepository interestRepository;

    @Mock
    private PushTestService pushTestService;

    @Test
    void notifiesOnlyActiveEligibleInterestAfterCommit() throws Exception {
        AvailabilityPushNotifier notifier = new AvailabilityPushNotifier(interestRepository, pushTestService);
        AvailabilityInterestEntity active = interest(AvailabilityInterestStatus.ACTIVE);
        AvailabilityInterestEntity canceled = interest(AvailabilityInterestStatus.CANCELED);
        when(interestRepository.findEligibleInterests(
                AvailabilityInterestStatus.ACTIVE, AppointmentStatus.SCHEDULED, BARBER_ID, SLOT_TIME))
                .thenReturn(List.of(active, canceled));

        notifier.notifyEligibleCustomers(new AvailableSlotCreatedEvent(BARBER_ID, SLOT_TIME, AVAILABLE_SLOT_ID));

        verify(pushTestService).sendAvailabilityNotification(
                CUSTOMER_ID, active.getId(), AVAILABLE_SLOT_ID);
        verify(pushTestService, never()).sendAvailabilityNotification(
                canceled.getCustomer().getId(), canceled.getId(), AVAILABLE_SLOT_ID);
        TransactionalEventListener annotation = AvailabilityPushNotifier.class
                .getMethod("notifyEligibleCustomers", AvailableSlotCreatedEvent.class)
                .getAnnotation(TransactionalEventListener.class);
        assertEquals(TransactionPhase.AFTER_COMMIT, annotation.phase());
    }

    @Test
    void pushFailureDoesNotPropagateToTheCancellationTransaction() {
        AvailabilityPushNotifier notifier = new AvailabilityPushNotifier(interestRepository, pushTestService);
        when(interestRepository.findEligibleInterests(
                AvailabilityInterestStatus.ACTIVE, AppointmentStatus.SCHEDULED, BARBER_ID, SLOT_TIME))
                .thenReturn(List.of(interest(AvailabilityInterestStatus.ACTIVE)));
        doThrow(new BusinessException("push indisponível"))
                .when(pushTestService).sendAvailabilityNotification(
                        CUSTOMER_ID, INTEREST_A_ID, AVAILABLE_SLOT_ID);

        assertDoesNotThrow(() -> notifier.notifyEligibleCustomers(
                new AvailableSlotCreatedEvent(BARBER_ID, SLOT_TIME, AVAILABLE_SLOT_ID)));
    }

    @Test
    void notifiesTwoActiveInterestsOfTheSameCustomerIndependently() {
        AvailabilityPushNotifier notifier = new AvailabilityPushNotifier(interestRepository, pushTestService);
        AvailabilityInterestEntity first = interest(AvailabilityInterestStatus.ACTIVE, INTEREST_A_ID);
        AvailabilityInterestEntity second = interest(AvailabilityInterestStatus.ACTIVE, INTEREST_B_ID);
        when(interestRepository.findEligibleInterests(
                AvailabilityInterestStatus.ACTIVE, AppointmentStatus.SCHEDULED, BARBER_ID, SLOT_TIME))
                .thenReturn(List.of(first, second));

        notifier.notifyEligibleCustomers(new AvailableSlotCreatedEvent(BARBER_ID, SLOT_TIME, AVAILABLE_SLOT_ID));

        verify(pushTestService).sendAvailabilityNotification(
                CUSTOMER_ID, INTEREST_A_ID, AVAILABLE_SLOT_ID);
        verify(pushTestService).sendAvailabilityNotification(
                CUSTOMER_ID, INTEREST_B_ID, AVAILABLE_SLOT_ID);
    }

    private AvailabilityInterestEntity interest(AvailabilityInterestStatus status) {
        return interest(status, status == AvailabilityInterestStatus.ACTIVE
                ? INTEREST_A_ID : UUID.fromString("00000000-0000-0000-0000-000000000003"));
    }

    private AvailabilityInterestEntity interest(
            AvailabilityInterestStatus status, UUID interestId) {
        CustomerEntity customer = new CustomerEntity("Cliente", "(11) 98888-8888", "cliente@example.com", null, null, true);
        ReflectionTestUtils.setField(customer, "id", status == AvailabilityInterestStatus.CANCELED
                ? UUID.fromString("00000000-0000-0000-0000-000000000003") : CUSTOMER_ID);
        BarberEntity barber = new BarberEntity("Barbeiro", "barbeiro@example.com", "(11) 99999-9999", "Corte", true);
        ReflectionTestUtils.setField(barber, "id", BARBER_ID);
        AppointmentEntity appointment = new AppointmentEntity(customer, barber,
                List.of(new ServiceEntity("Corte", "", 30, BigDecimal.TEN, true)), BigDecimal.TEN,
                SLOT_TIME.plusDays(1), AppointmentStatus.SCHEDULED, "");
        AvailabilityInterestEntity interest = new AvailabilityInterestEntity(customer, appointment);
        interest.setId(interestId);
        interest.setStatus(status);
        return interest;
    }
}
