package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.CreatePublicAppointmentRequest;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicAppointmentServiceTest {
    private static final UUID BARBER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Mock private AppointmentService appointmentService;
    @Mock private CustomerRepository customerRepository;
    @Mock private BarberRepository barberRepository;

    @Test
    void createsAnActiveCustomerWhenPhoneIsUnknown() {
        PublicAppointmentService service = new PublicAppointmentService(appointmentService, customerRepository, barberRepository);
        CreatePublicAppointmentRequest request = new CreatePublicAppointmentRequest();
        request.setCustomerName("Ana");
        request.setCustomerPhone("(11) 99999-9999");
        request.setServiceIds(List.of(UUID.randomUUID()));
        request.setAppointmentDateTime(LocalDateTime.of(2026, 9, 9, 10, 0));

        BarberEntity barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        BarbershopEntity shop = new BarbershopEntity(UUID.randomUUID(), "Jhow Cortes", true);
        barber.setBarbershop(shop);
        when(barberRepository.findById(BARBER_ID)).thenReturn(Optional.of(barber));
        when(customerRepository.findByBarbershopIdAndPhone(shop.getId(), request.getCustomerPhone())).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity customer = invocation.getArgument(0);
            ReflectionTestUtils.setField(customer, "id", CUSTOMER_ID);
            return customer;
        });
        when(appointmentService.createPublic(eq(BARBER_ID), any(CreateAppointmentRequest.class)))
                .thenReturn(new PublicAppointmentResponse());

        service.createPublicAppointment(BARBER_ID, request);

        ArgumentCaptor<CustomerEntity> customer = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(customer.capture());
        assertTrue(customer.getValue().getActive());
    }
}
