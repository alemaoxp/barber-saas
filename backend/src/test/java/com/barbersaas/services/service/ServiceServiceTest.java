package com.barbersaas.services.service;

import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import com.barbersaas.services.repository.ServiceRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceServiceTest {

    @Test
    void findActiveShouldExcludeInactiveServices() {
        ServiceRepository repository = mock(ServiceRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        UUID barberId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        barber.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        when(barbers.findById(barberId)).thenReturn(Optional.of(barber));
        when(repository.findByBarbershopIdAndActiveTrue(shopId)).thenReturn(List.of(
                new ServiceEntity("Corte", "", 30, BigDecimal.valueOf(40), true),
                new ServiceEntity("Inativo", "", 30, BigDecimal.valueOf(30), false)
        ));

        var responses = new ServiceService(repository, new ServiceMapper(), barbers).findActive(barberId);

        assertEquals(List.of("Corte"), responses.stream().map(response -> response.getName()).toList());
    }
}
