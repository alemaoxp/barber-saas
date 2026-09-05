package com.barbersaas.availableslot.service;

import com.barbersaas.availableslot.entity.AvailableSlotEntity;
import com.barbersaas.availableslot.enums.AvailableSlotStatus;
import com.barbersaas.availableslot.repository.AvailableSlotRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailableSlotServiceTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private AvailableSlotRepository availableSlotRepository;

    private AvailableSlotService availableSlotService;
    private BarberEntity barber;

    @BeforeEach
    void setUp() {
        availableSlotService =
                new AvailableSlotService(availableSlotRepository);

        barber = new BarberEntity(
                "Barbeiro",
                "barbeiro@example.com",
                "(11) 99999-9999",
                "Corte",
                true
        );
        ReflectionTestUtils.setField(barber, "id", BARBER_ID);
    }

    @Test
    void registerAvailableSlotShouldCreateAvailableSlot() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        when(availableSlotRepository.findByBarberIdAndAvailableDateTime(
                BARBER_ID,
                dateTime
        )).thenReturn(Optional.empty());
        when(availableSlotRepository.save(any(AvailableSlotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AvailableSlotEntity slot =
                availableSlotService.registerAvailableSlot(
                        barber,
                        dateTime
                );

        assertSame(barber, slot.getBarber());
        assertEquals(dateTime, slot.getAvailableDateTime());
        assertEquals(AvailableSlotStatus.AVAILABLE, slot.getStatus());
        verify(availableSlotRepository).save(slot);
    }

    @Test
    void registerAvailableSlotShouldNotCreateDuplicate() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        AvailableSlotEntity existingSlot =
                new AvailableSlotEntity(barber, dateTime);

        when(availableSlotRepository.findByBarberIdAndAvailableDateTime(
                BARBER_ID,
                dateTime
        )).thenReturn(Optional.of(existingSlot));

        AvailableSlotEntity slot =
                availableSlotService.registerAvailableSlot(
                        barber,
                        dateTime
                );

        assertSame(existingSlot, slot);
        verify(availableSlotRepository, never()).save(any());
    }
}
