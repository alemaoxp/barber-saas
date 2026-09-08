package com.barbersaas.push.service;

import com.barbersaas.appointments.event.AvailableSlotCreatedEvent;
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.availabilityinterest.repository.AvailabilityInterestRepository;
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

    public AvailabilityPushNotifier(
            AvailabilityInterestRepository interestRepository,
            PushTestService pushTestService) {
        this.interestRepository = interestRepository;
        this.pushTestService = pushTestService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyEligibleCustomers(AvailableSlotCreatedEvent event) {
        for (AvailabilityInterestEntity interest : interestRepository.findEligibleInterests(
                AvailabilityInterestStatus.ACTIVE, event.barberId(), event.availableDateTime())) {
            if (interest.getStatus() != AvailabilityInterestStatus.ACTIVE) {
                continue;
            }
            try {
                pushTestService.sendAvailabilityNotification(interest.getCustomer().getId());
            } catch (RuntimeException exception) {
                LOGGER.warn("Não foi possível alertar o cliente {} sobre a vaga {}.",
                        interest.getCustomer().getId(), event.availableDateTime(), exception);
            }
        }
    }
}
