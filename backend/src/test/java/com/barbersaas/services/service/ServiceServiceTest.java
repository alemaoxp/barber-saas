package com.barbersaas.services.service;

import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import com.barbersaas.services.repository.ServiceRepository;
import com.barbersaas.services.dto.UpdateServiceRequest;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Test
    void findAllShouldReturnOnlyActiveServicesForAdmin() {
        ServiceRepository repository = mock(ServiceRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        UUID barberId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        barber.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        when(barbers.findById(barberId)).thenReturn(Optional.of(barber));
        when(repository.findByBarbershopIdAndActiveTrue(shopId)).thenReturn(List.of(
                new ServiceEntity("Corte", "", 30, BigDecimal.valueOf(40), true)
        ));

        var responses = new ServiceService(repository, new ServiceMapper(), barbers).findAll(barberId);

        assertEquals(List.of("Corte"), responses.stream().map(response -> response.getName()).toList());
        verify(repository, never()).findByBarbershopId(shopId);
    }

    @Test
    void deleteShouldDeactivateServiceWithoutPhysicalDelete() {
        ServiceRepository repository = mock(ServiceRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        UUID barberId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        barber.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        ServiceEntity service = new ServiceEntity("Corte", "", 30, BigDecimal.valueOf(40), true);
        service.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        when(barbers.findById(barberId)).thenReturn(Optional.of(barber));
        when(repository.findById(serviceId)).thenReturn(Optional.of(service));

        new ServiceService(repository, new ServiceMapper(), barbers).delete(barberId, serviceId);

        assertEquals(false, service.getActive());
        verify(repository).save(service);
        verify(repository, never()).delete(service);
    }

    @Test
    void updateShouldChangeOnlyNameAndPriceKeepingAdminHiddenFields() {
        ServiceRepository repository = mock(ServiceRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        UUID barberId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        barber.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        ServiceEntity service = new ServiceEntity("Corte", "Descricao original", 45, BigDecimal.valueOf(40), false);
        service.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        ReflectionTestUtils.setField(service, "id", serviceId);
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Corte Premium");
        request.setPrice(BigDecimal.valueOf(55));
        request.setDescription("Descricao alterada");
        request.setDurationMinutes(10);
        request.setActive(true);
        when(barbers.findById(barberId)).thenReturn(Optional.of(barber));
        when(repository.findById(serviceId)).thenReturn(Optional.of(service));
        when(repository.save(service)).thenReturn(service);

        var response = new ServiceService(repository, new ServiceMapper(), barbers)
                .update(barberId, serviceId, request);

        assertEquals("Corte Premium", response.getName());
        assertEquals(BigDecimal.valueOf(55), response.getPrice());
        assertEquals("Descricao original", service.getDescription());
        assertEquals(45, service.getDurationMinutes());
        assertEquals(false, service.getActive());
        verify(repository).save(service);
    }

    @Test
    void updateShouldRejectServiceFromAnotherBarbershop() {
        ServiceRepository repository = mock(ServiceRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        UUID barberId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        barber.setBarbershop(new BarbershopEntity(shopId, "Jhow Cortes", true));
        ServiceEntity service = new ServiceEntity("Corte", "", 30, BigDecimal.valueOf(40), true);
        service.setBarbershop(new BarbershopEntity(UUID.randomUUID(), "Outra", true));
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Corte Premium");
        request.setPrice(BigDecimal.valueOf(55));
        when(barbers.findById(barberId)).thenReturn(Optional.of(barber));
        when(repository.findById(serviceId)).thenReturn(Optional.of(service));

        assertThrows(RuntimeException.class, () ->
                new ServiceService(repository, new ServiceMapper(), barbers)
                        .update(barberId, serviceId, request));
        verify(repository, never()).save(service);
    }
}
