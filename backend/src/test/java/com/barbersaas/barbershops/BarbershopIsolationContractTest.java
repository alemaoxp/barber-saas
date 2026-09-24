package com.barbersaas.barbershops;

import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.availabilityinterest.dto.CreateAvailabilityInterestRequest;
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import com.barbersaas.availabilityinterest.enums.AvailabilityInterestStatus;
import com.barbersaas.availabilityinterest.mapper.AvailabilityInterestMapper;
import com.barbersaas.availabilityinterest.repository.AvailabilityInterestRepository;
import com.barbersaas.availabilityinterest.service.AvailabilityInterestService;
import com.barbersaas.availableslot.repository.AvailableSlotRepository;
import com.barbersaas.availableslot.service.AvailableSlotService;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.mapper.BarberMapper;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbers.service.BarberService;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import com.barbersaas.barbershops.repository.BarbershopRepository;
import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import com.barbersaas.push.service.AvailabilityPushNotifier;
import com.barbersaas.push.service.WebPushService;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import com.barbersaas.services.repository.ServiceRepository;
import com.barbersaas.services.service.ServiceService;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.mapper.CustomerMapper;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.customers.service.CustomerService;
import com.barbersaas.appointments.event.AvailableSlotCreatedEvent;
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import com.barbersaas.auth.entity.AdminUserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Contract tests for the first multi-tenant boundary.
 *
 * These tests intentionally include the current gaps. A failing test is a
 * production isolation hole to address in a later stage, not a reason to
 * weaken the assertion here.
 */
@ExtendWith(MockitoExtension.class)
class BarbershopIsolationContractTest {

    private static final UUID SHOP_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SHOP_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID BARBER_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID BARBER_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final UUID CUSTOMER_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");
    private static final UUID CUSTOMER_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000004");
    private static final UUID SERVICE_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000005");
    private static final UUID SERVICE_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000005");
    private static final UUID APPOINTMENT_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000006");
    private static final UUID APPOINTMENT_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000006");
    private static final UUID INTEREST_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000007");
    private static final UUID SLOT_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000008");
    private static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.now().plusDays(3);

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private BarbershopRepository barbershopRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityInterestRepository availabilityInterestRepository;

    @Mock
    private AvailableSlotService availableSlotService;

    @Mock
    private AvailableSlotRepository availableSlotRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private WebPushService pushTestService;

    @Test
    void adminBarberCannotReadCustomerFromAnotherBarbershop() {
        TenantFixture fixture = fixture();
        when(barberRepository.existsById(BARBER_A_ID)).thenReturn(true);
        when(barberRepository.findById(BARBER_A_ID)).thenReturn(Optional.of(fixture.barberA));
        when(customerRepository.findById(CUSTOMER_B_ID)).thenReturn(Optional.of(fixture.customerB));

        CustomerService service = new CustomerService(
                customerRepository,
                new CustomerMapper(),
                barberRepository
        );

        assertThrows(NotFoundException.class,
                () -> service.findById(BARBER_A_ID, CUSTOMER_B_ID));
        verify(customerRepository).findById(CUSTOMER_B_ID);
    }

    @Test
    void adminBarberCannotReadServiceFromAnotherBarbershop() {
        TenantFixture fixture = fixture();
        when(barberRepository.findById(BARBER_A_ID)).thenReturn(Optional.of(fixture.barberA));
        when(serviceRepository.findById(SERVICE_B_ID)).thenReturn(Optional.of(fixture.serviceB));

        ServiceService service = new ServiceService(
                serviceRepository,
                new ServiceMapper(),
                barberRepository
        );

        assertThrows(RuntimeException.class,
                () -> service.findById(BARBER_A_ID, SERVICE_B_ID));
        verify(serviceRepository).findById(SERVICE_B_ID);
    }

    @Test
    void barberCollectionMustReturnOnlyTheAuthenticatedBarbershop() {
        TenantFixture fixture = fixture();
        when(barberRepository.findByBarbershopId(SHOP_A_ID)).thenReturn(List.of(fixture.barberA));
        when(barberRepository.findByBarbershopId(SHOP_B_ID)).thenReturn(List.of(fixture.barberB));

        BarberService service = new BarberService(
                barberRepository,
                new BarberMapper(),
                org.mockito.Mockito.mock(BarberScheduleRepository.class),
                org.mockito.Mockito.mock(WeeklyScheduleRepository.class),
                barbershopRepository
        );

        assertEquals(1, service.findAll(SHOP_A_ID).size());
        assertEquals(1, service.findAll(SHOP_B_ID).size());
        verify(barberRepository).findByBarbershopId(SHOP_A_ID);
        verify(barberRepository).findByBarbershopId(SHOP_B_ID);
        verify(barberRepository, never()).findAll();
    }

    @Test
    void administrativeBarberCreationUsesAuthenticatedBarbershop() {
        TenantFixture fixture = fixture();
        BarberScheduleRepository barberScheduleRepository = org.mockito.Mockito.mock(BarberScheduleRepository.class);
        when(barbershopRepository.findById(SHOP_A_ID)).thenReturn(Optional.of(fixture.shopA));
        when(barberScheduleRepository.existsByBarberId(any())).thenReturn(true);
        when(barberRepository.save(any(BarberEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateBarberRequest request = new CreateBarberRequest();
        request.setName("Barber A2");
        request.setEmail("barber-a2@example.com");
        request.setPhone("(11) 90000-0003");
        request.setActive(true);

        BarberService service = new BarberService(
                barberRepository,
                new BarberMapper(),
                barberScheduleRepository,
                org.mockito.Mockito.mock(WeeklyScheduleRepository.class),
                barbershopRepository
        );

        service.create(SHOP_A_ID, request);

        verify(barbershopRepository).findById(SHOP_A_ID);
        verify(barberRepository).save(argThat(barber -> fixture.shopA.equals(barber.getBarbershop())));
    }

    @Test
    void scopedBarberLookupCannotReadAnotherBarbershop() {
        when(barberRepository.findByIdAndBarbershopId(BARBER_B_ID, SHOP_A_ID))
                .thenReturn(Optional.empty());

        BarberService service = new BarberService(
                barberRepository,
                new BarberMapper(),
                org.mockito.Mockito.mock(BarberScheduleRepository.class),
                org.mockito.Mockito.mock(WeeklyScheduleRepository.class),
                barbershopRepository
        );

        assertThrows(NotFoundException.class,
                () -> service.findById(SHOP_A_ID, BARBER_B_ID));
        verify(barberRepository, never()).findById(BARBER_B_ID);
    }

    @Test
    void appointmentCannotCombineBarberAWithCustomerB() {
        TenantFixture fixture = fixture();
        AppointmentService service = appointmentService(fixture);
        when(customerRepository.findById(CUSTOMER_B_ID)).thenReturn(Optional.of(fixture.customerB));

        assertThrows(BusinessException.class, () -> service.create(
                BARBER_A_ID,
                new CreateAppointmentRequest(CUSTOMER_B_ID, List.of(SERVICE_A_ID), APPOINTMENT_TIME, null)
        ));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void appointmentCannotCombineBarberAWithServiceB() {
        TenantFixture fixture = fixture();
        AppointmentService service = appointmentService(fixture);
        when(serviceRepository.findById(SERVICE_B_ID)).thenReturn(Optional.of(fixture.serviceB));

        assertThrows(BusinessException.class, () -> service.create(
                BARBER_A_ID,
                new CreateAppointmentRequest(CUSTOMER_A_ID, List.of(SERVICE_B_ID), APPOINTMENT_TIME, null)
        ));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void adminBarberCannotCancelAppointmentFromAnotherBarbershop() {
        TenantFixture fixture = fixture();
        AppointmentService service = appointmentService(fixture);
        when(appointmentRepository.findByIdAndBarberId(APPOINTMENT_B_ID, BARBER_A_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.delete(BARBER_A_ID, APPOINTMENT_B_ID));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void adminBarberCannotUpdateAppointmentFromAnotherBarbershop() {
        TenantFixture fixture = fixture();
        AppointmentService service = appointmentService(fixture);
        when(appointmentRepository.findByIdAndBarberId(APPOINTMENT_B_ID, BARBER_A_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(
                        BARBER_A_ID,
                        APPOINTMENT_B_ID,
                        new UpdateAppointmentRequest(
                                CUSTOMER_A_ID,
                                List.of(SERVICE_A_ID),
                                APPOINTMENT_TIME,
                                null
                        )));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void availabilityInterestCannotLinkCustomerAToAppointmentOwnedByBarberB() {
        TenantFixture fixture = fixture();
        AppointmentEntity crossTenantAppointment = new AppointmentEntity(
                fixture.customerA,
                fixture.barberB,
                List.of(fixture.serviceB),
                BigDecimal.valueOf(50),
                APPOINTMENT_TIME,
                AppointmentStatus.SCHEDULED,
                null
        );
        ReflectionTestUtils.setField(crossTenantAppointment, "id", APPOINTMENT_B_ID);

        when(customerRepository.findById(CUSTOMER_A_ID)).thenReturn(Optional.of(fixture.customerA));
        when(appointmentRepository.findById(APPOINTMENT_B_ID))
                .thenReturn(Optional.of(crossTenantAppointment));
        AvailabilityInterestService service = new AvailabilityInterestService(
                availabilityInterestRepository,
                appointmentRepository,
                availableSlotRepository,
                customerRepository,
                new AvailabilityInterestMapper(),
                org.mockito.Mockito.mock(AppointmentService.class)
        );
        CreateAvailabilityInterestRequest request = new CreateAvailabilityInterestRequest();
        request.setAppointmentId(APPOINTMENT_B_ID);

        assertThrows(BusinessException.class,
                () -> service.create(CUSTOMER_A_ID, request));
        verify(availabilityInterestRepository, never()).save(any());
    }

    @Test
    void availabilityInterestCannotAcceptSlotOwnedByAnotherBarbershop() {
        TenantFixture fixture = fixture();
        AvailabilityInterestEntity interestA = new AvailabilityInterestEntity(
                fixture.customerA,
                fixture.appointmentA
        );
        ReflectionTestUtils.setField(interestA, "id", UUID.randomUUID());
        interestA.setStatus(AvailabilityInterestStatus.ACTIVE);

        com.barbersaas.availableslot.entity.AvailableSlotEntity slotB =
                new com.barbersaas.availableslot.entity.AvailableSlotEntity(
                        fixture.barberB,
                        APPOINTMENT_TIME.minusDays(1)
                );
        ReflectionTestUtils.setField(slotB, "id", SLOT_B_ID);
        slotB.setCreatedAt(LocalDateTime.now());

        when(availabilityInterestRepository.findByIdForUpdate(interestA.getId()))
                .thenReturn(Optional.of(interestA));
        when(availableSlotRepository.findByIdForUpdate(SLOT_B_ID))
                .thenReturn(Optional.of(slotB));
        when(appointmentRepository.findByIdForUpdate(APPOINTMENT_A_ID))
                .thenReturn(Optional.of(fixture.appointmentA));

        AvailabilityInterestService service = new AvailabilityInterestService(
                availabilityInterestRepository,
                appointmentRepository,
                availableSlotRepository,
                customerRepository,
                new AvailabilityInterestMapper(),
                org.mockito.Mockito.mock(AppointmentService.class)
        );

        assertThrows(BusinessException.class, () -> service.accept(
                CUSTOMER_A_ID,
                interestA.getId(),
                new com.barbersaas.availabilityinterest.dto.AcceptAvailabilityInterestRequest(SLOT_B_ID)
        ));
    }

    @Test
    void notifierMustNotSendBarbershopBInterestForBarbershopASlot() {
        TenantFixture fixture = fixture();
        AvailabilityInterestEntity interestB = new AvailabilityInterestEntity(
                fixture.customerB,
                fixture.appointmentB
        );
        interestB.setId(INTEREST_B_ID);
        interestB.setStatus(AvailabilityInterestStatus.ACTIVE);
        AvailableSlotCreatedEvent event = new AvailableSlotCreatedEvent(
                BARBER_A_ID,
                APPOINTMENT_TIME.minusDays(1),
                SLOT_B_ID
        );
        when(availabilityInterestRepository.findEligibleInterests(
                AvailabilityInterestStatus.ACTIVE,
                AppointmentStatus.SCHEDULED,
                BARBER_A_ID,
                event.availableDateTime()
        )).thenReturn(List.of(interestB));

        when(barberRepository.findById(BARBER_A_ID)).thenReturn(Optional.of(fixture.barberA));

        new AvailabilityPushNotifier(availabilityInterestRepository, pushTestService, barberRepository)
                .notifyEligibleCustomers(event);

        verifyNoInteractions(pushTestService);
    }

    private AppointmentService appointmentService(TenantFixture fixture) {
        when(barberRepository.findById(BARBER_A_ID)).thenReturn(Optional.of(fixture.barberA));
        org.mockito.Mockito.lenient()
                .when(customerRepository.findById(CUSTOMER_A_ID))
                .thenReturn(Optional.of(fixture.customerA));
        org.mockito.Mockito.lenient()
                .when(serviceRepository.findById(SERVICE_A_ID))
                .thenReturn(Optional.of(fixture.serviceA));
        return new AppointmentService(
                appointmentRepository,
                customerRepository,
                barberRepository,
                serviceRepository,
                new com.barbersaas.appointments.mapper.AppointmentMapper(),
                org.mockito.Mockito.mock(com.barbersaas.scheduleblock.service.ScheduleBlockService.class),
                org.mockito.Mockito.mock(com.barbersaas.weeklyschedule.service.WeeklyScheduleService.class),
                availableSlotService,
                eventPublisher
        );
    }

    private TenantFixture fixture() {
        BarbershopEntity shopA = new BarbershopEntity(SHOP_A_ID, "Jhow Cortes A", true);
        BarbershopEntity shopB = new BarbershopEntity(SHOP_B_ID, "Jhow Cortes B", true);

        AdminUserEntity adminA = new AdminUserEntity(shopA, "Admin A", "admin-a@example.com", "hash-a", true);
        ReflectionTestUtils.setField(adminA, "id", ADMIN_A_ID);
        AdminUserEntity adminB = new AdminUserEntity(shopB, "Admin B", "admin-b@example.com", "hash-b", true);
        ReflectionTestUtils.setField(adminB, "id", ADMIN_B_ID);

        BarberEntity barberA = new BarberEntity("Barber A", "barber-a@example.com", "(11) 90000-0001", null, true);
        ReflectionTestUtils.setField(barberA, "id", BARBER_A_ID);
        barberA.setBarbershop(shopA);
        BarberEntity barberB = new BarberEntity("Barber B", "barber-b@example.com", "(11) 90000-0002", null, true);
        ReflectionTestUtils.setField(barberB, "id", BARBER_B_ID);
        barberB.setBarbershop(shopB);

        CustomerEntity customerA = new CustomerEntity("Customer A", "(11) 98888-0001", "customer-a@example.com", null, null, true);
        ReflectionTestUtils.setField(customerA, "id", CUSTOMER_A_ID);
        customerA.setBarbershop(shopA);
        CustomerEntity customerB = new CustomerEntity("Customer B", "(11) 98888-0002", "customer-b@example.com", null, null, true);
        ReflectionTestUtils.setField(customerB, "id", CUSTOMER_B_ID);
        customerB.setBarbershop(shopB);

        ServiceEntity serviceA = new ServiceEntity("Service A", null, 30, BigDecimal.valueOf(40), true);
        ReflectionTestUtils.setField(serviceA, "id", SERVICE_A_ID);
        serviceA.setBarbershop(shopA);
        ServiceEntity serviceB = new ServiceEntity("Service B", null, 30, BigDecimal.valueOf(50), true);
        ReflectionTestUtils.setField(serviceB, "id", SERVICE_B_ID);
        serviceB.setBarbershop(shopB);

        AppointmentEntity appointmentA = appointment(APPOINTMENT_A_ID, customerA, barberA, serviceA);
        AppointmentEntity appointmentB = appointment(APPOINTMENT_B_ID, customerB, barberB, serviceB);
        return new TenantFixture(
                shopA, shopB, adminA, adminB, barberA, barberB,
                customerA, customerB, serviceA, serviceB, appointmentA, appointmentB
        );
    }

    private AppointmentEntity appointment(
            UUID id,
            CustomerEntity customer,
            BarberEntity barber,
            ServiceEntity service) {
        AppointmentEntity appointment = new AppointmentEntity(
                customer,
                barber,
                List.of(service),
                service.getPrice(),
                APPOINTMENT_TIME,
                AppointmentStatus.SCHEDULED,
                null
        );
        ReflectionTestUtils.setField(appointment, "id", id);
        return appointment;
    }

    private record TenantFixture(
            BarbershopEntity shopA,
            BarbershopEntity shopB,
            AdminUserEntity adminA,
            AdminUserEntity adminB,
            BarberEntity barberA,
            BarberEntity barberB,
            CustomerEntity customerA,
            CustomerEntity customerB,
            ServiceEntity serviceA,
            ServiceEntity serviceB,
            AppointmentEntity appointmentA,
            AppointmentEntity appointmentB
    ) { }
}
