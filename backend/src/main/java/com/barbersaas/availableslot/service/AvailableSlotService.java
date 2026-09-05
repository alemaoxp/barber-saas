package com.barbersaas.availableslot.service;

import com.barbersaas.availableslot.entity.AvailableSlotEntity;
import com.barbersaas.availableslot.enums.AvailableSlotStatus;
import com.barbersaas.availableslot.repository.AvailableSlotRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AvailableSlotService {

    private final AvailableSlotRepository availableSlotRepository;

    public AvailableSlotService(
            AvailableSlotRepository availableSlotRepository) {

        this.availableSlotRepository =
                availableSlotRepository;
    }

    @Transactional
    public AvailableSlotEntity reserve(UUID slotId) {

        AvailableSlotEntity slot =
                availableSlotRepository.findByIdForUpdate(slotId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Vaga de antecipação não encontrada."
                                )
                        );

        if (slot.getStatus() != AvailableSlotStatus.AVAILABLE) {
            throw new BusinessException(
                    "Esta vaga não está mais disponível."
            );
        }

        slot.setStatus(
                AvailableSlotStatus.RESERVED
        );

        return availableSlotRepository.save(slot);
    }

    @Transactional
    public AvailableSlotEntity registerAvailableSlot(
            BarberEntity barber,
            LocalDateTime availableDateTime) {

        return availableSlotRepository
                .findByBarberIdAndAvailableDateTime(
                        barber.getId(),
                        availableDateTime
                )
                .orElseGet(() ->
                        availableSlotRepository.save(
                                new AvailableSlotEntity(
                                        barber,
                                        availableDateTime
                                )
                        )
                );
    }
}
