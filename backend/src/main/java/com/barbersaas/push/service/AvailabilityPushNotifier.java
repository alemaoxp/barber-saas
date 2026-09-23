package com.barbersaas.push.service;

import com.barbersaas.appointments.event.AvailableSlotCreatedEvent;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.availabilityinterest.repository.AvailabilityInterestRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AvailabilityPushNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvailabilityPushNotifier.class);

    private final AvailabilityInterestRepository interestRepository;
    private final PushTestService pushTestService;
    private final BarberRepository barberRepository;

    public AvailabilityPushNotifier(
            AvailabilityInterestRepository interestRepository,
            PushTestService pushTestService,
            BarberRepository barberRepository) {
        this.interestRepository = interestRepository;
        this.pushTestService = pushTestService;
        this.barberRepository = barberRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyEligibleCustomers(AvailableSlotCreatedEvent event) {
        if (event.barberId() == null) {
            return;
        }

        BarberEntity eventBarber = barberRepository.findById(event.barberId())
                .orElse(null);
        if (eventBarber == null || eventBarber.getBarbershop() == null
                || eventBarber.getBarbershop().getId() == null) {
            LOGGER.warn("Ignorando notificação de vaga sem barbeiro/barbearia válida: {}.",
                    event.barberId());
            return;
        }

        for (AvailabilityInterestEntity interest : interestRepository.findEligibleInterests(
                AvailabilityInterestStatus.ACTIVE,
                AppointmentStatus.SCHEDULED,
                event.barberId(),
                event.availableDateTime())) {
            if (interest.getStatus() != AvailabilityInterestStatus.ACTIVE) {
                continue;
            }
            if (!belongsToEventBarbershop(eventBarber, interest, event)) {
                LOGGER.debug("Ignorando interesse {} de barbearia incompatível com a vaga {}.",
                        interest.getId(), event.availableSlotId());
                continue;
            }
            try {
                pushTestService.sendAvailabilityNotification(
                        interest.getCustomer().getId(),
                        interest.getId(),
                        event.availableSlotId());
            } catch (RuntimeException exception) {
                LOGGER.warn("Não foi possível alertar o cliente {} sobre a vaga {}.",
                        interest.getCustomer().getId(), event.availableDateTime(), exception);
            }
        }
    }

    private boolean belongsToEventBarbershop(
            BarberEntity eventBarber,
            AvailabilityInterestEntity interest,
            AvailableSlotCreatedEvent event) {
        if (interest == null || interest.getCustomer() == null
                || interest.getCustomer().getId() == null
                || interest.getAppointment() == null
                || interest.getAppointment().getBarber() == null) {
            return false;
        }

        BarberEntity interestBarber = interest.getAppointment().getBarber();
        return event.barberId().equals(interestBarber.getId())
                && interestBarber.getBarbershop() != null
                && eventBarber.getBarbershop().getId()
                .equals(interestBarber.getBarbershop().getId());
    }
}
