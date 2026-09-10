package com.barbersaas.barbershops;

import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BarbershopIsolationTest {
    private static final UUID BARBER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID SERVICE_ID = UUID.randomUUID();

    @Test
    void customerPhoneIsScopedToBarbershop() {
        CustomerRepository repository = mock(CustomerRepository.class);
        UUID shopA = UUID.randomUUID();
        UUID shopB = UUID.randomUUID();
        CustomerEntity customerA = customer("(11) 99999-9999", shopA);
        CustomerEntity customerB = customer("(11) 99999-9999", shopB);

        when(repository.findByBarbershopIdAndPhone(shopA, customerA.getPhone()))
                .thenReturn(Optional.of(customerA));
        when(repository.findByBarbershopIdAndPhone(shopB, customerB.getPhone()))
                .thenReturn(Optional.of(customerB));

        assertEquals(customerA, repository.findByBarbershopIdAndPhone(shopA, customerA.getPhone()).orElseThrow());
        assertEquals(customerB, repository.findByBarbershopIdAndPhone(shopB, customerB.getPhone()).orElseThrow());
    }

    @Test
    void appointmentRejectsCustomerFromAnotherBarbershop() {
        assertInvalidOwnership(customer(UUID.randomUUID()), service(UUID.randomUUID()));
    }

    @Test
    void appointmentRejectsAnyServiceFromAnotherBarbershopOnCreateAndUpdate() {
        AppointmentService service = appointmentService(customer(SHOP_A), service(UUID.randomUUID()));
        CreateAppointmentRequest create = new CreateAppointmentRequest(CUSTOMER_ID, List.of(SERVICE_ID), dateTime(), null);
        UpdateAppointmentRequest update = new UpdateAppointmentRequest(CUSTOMER_ID, List.of(SERVICE_ID), dateTime(), null);

        assertThrows(BusinessException.class, () -> service.create(BARBER_ID, create));
        assertThrows(BusinessException.class, () -> service.update(BARBER_ID, UUID.randomUUID(), update));
    }

    private static final UUID SHOP_A = UUID.randomUUID();

    private void assertInvalidOwnership(CustomerEntity customer, ServiceEntity service) {
        AppointmentService appointmentService = appointmentService(customer, service);
        assertThrows(BusinessException.class, () -> appointmentService.create(
                BARBER_ID, new CreateAppointmentRequest(CUSTOMER_ID, List.of(SERVICE_ID), dateTime(), null)));
    }

    private AppointmentService appointmentService(CustomerEntity customer, ServiceEntity... services) {
        BarberRepository barbers = mock(BarberRepository.class);
        CustomerRepository customers = mock(CustomerRepository.class);
        ServiceRepository serviceRepository = mock(ServiceRepository.class);
        BarberEntity barber = barber(SHOP_A);
        when(barbers.findById(BARBER_ID)).thenReturn(Optional.of(barber));
        when(customers.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(services[0]));
        var appointments = mock(com.barbersaas.appointments.repository.AppointmentRepository.class);
        com.barbersaas.appointments.entity.AppointmentEntity appointment = new com.barbersaas.appointments.entity.AppointmentEntity();
        ReflectionTestUtils.setField(appointment, "id", UUID.randomUUID());
        when(appointments.findByIdAndBarberId(any(), org.mockito.ArgumentMatchers.eq(BARBER_ID))).thenReturn(Optional.of(appointment));
        return new AppointmentService(appointments, customers, barbers, serviceRepository, new com.barbersaas.appointments.mapper.AppointmentMapper(), mock(), mock(), mock(), mock());
    }

    private static BarberEntity barber(UUID shopId) {
        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        ReflectionTestUtils.setField(barber, "id", BARBER_ID);
        barber.setBarbershop(shop(shopId));
        return barber;
    }

    private static CustomerEntity customer(UUID shopId) {
        return customer("(11) 99999-9999", shopId);
    }

    private static CustomerEntity customer(String phone, UUID shopId) {
        CustomerEntity customer = new CustomerEntity("Cliente", phone, null, null, null, true);
        customer.setBarbershop(shop(shopId));
        return customer;
    }

    private static ServiceEntity service(UUID shopId) {
        ServiceEntity service = new ServiceEntity("Corte", null, 30, BigDecimal.TEN, true);
        service.setBarbershop(shop(shopId));
        return service;
    }

    private static BarbershopEntity shop(UUID id) {
        return new BarbershopEntity(id, "Jhow Cortes", true);
    }

    private static LocalDateTime dateTime() {
        return LocalDateTime.of(2026, 9, 2, 10, 0);
    }
}
