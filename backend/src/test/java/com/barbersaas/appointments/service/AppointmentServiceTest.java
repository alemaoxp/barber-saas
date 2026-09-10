package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.DailyAgendaResponse;
import com.barbersaas.appointments.dto.DailyAgendaSlotStatus;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.mapper.AppointmentMapper;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.availableslot.service.AvailableSlotService;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.scheduleblock.entity.ScheduleBlockEntity;
import com.barbersaas.scheduleblock.service.ScheduleBlockService;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.repository.ServiceRepository;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.service.WeeklyScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    private static final UUID BARBERSHOP_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SERVICE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID SECOND_SERVICE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID APPOINTMENT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ScheduleBlockService scheduleBlockService;

    @Mock
    private WeeklyScheduleService weeklyScheduleService;

    @Mock
    private AvailableSlotService availableSlotService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AppointmentService appointmentService;
    private BarberEntity barber;
    private CustomerEntity customer;
    private ServiceEntity service40Minutes;
    private ServiceEntity service30Minutes;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                customerRepository,
                barberRepository,
                serviceRepository,
                new AppointmentMapper(),
                scheduleBlockService,
                weeklyScheduleService,
                availableSlotService,
                eventPublisher
        );

        barber = barber();
        customer = customer();
        service40Minutes = service(SERVICE_ID, 40, 50);
        service30Minutes = service(SECOND_SERVICE_ID, 30, 30);

        lenient().when(barberRepository.findById(BARBER_ID))
                .thenReturn(Optional.of(barber));
        lenient().when(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customer));
        lenient().when(serviceRepository.findById(SERVICE_ID))
                .thenReturn(Optional.of(service40Minutes));
        lenient().when(serviceRepository.findById(SECOND_SERVICE_ID))
                .thenReturn(Optional.of(service30Minutes));
        lenient().when(appointmentRepository.save(any(AppointmentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                any(),
                any()
        )).thenReturn(List.of());
    }

    @Test
    void mondayAt0930ShouldBeAllowed() {
        assertCreateAllowed(LocalDateTime.of(2026, 8, 31, 9, 30));
    }

    @Test
    void mondayAt2000ShouldBeAllowed() {
        assertCreateAllowed(LocalDateTime.of(2026, 8, 31, 20, 0));
    }

    @Test
    void mondayAt2001ShouldBeRejectedAsOutsideBusinessHours() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 31, 20, 1);
        doThrow(new BusinessException("Horário fora do expediente."))
                .when(weeklyScheduleService)
                .validateWorkingHours(BARBER_ID, dateTime);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createAt(dateTime)
        );

        assertEquals("Horário fora do expediente.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void mondayAt1130WithServiceCrossingNoonShouldBeAllowed() {
        assertCreateAllowed(LocalDateTime.of(2026, 8, 31, 11, 30));
    }

    @Test
    void mondayAt1300ShouldBeRejectedAsInsideBreak() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 31, 13, 0);
        doThrow(new BusinessException("Horário dentro do intervalo."))
                .when(weeklyScheduleService)
                .validateWorkingHours(BARBER_ID, dateTime);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createAt(dateTime)
        );

        assertEquals("Horário dentro do intervalo.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void tuesdayShouldBeRejectedBecauseBarberDoesNotWork() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 1, 9, 30);
        doThrow(new BusinessException("Barbeiro não atende neste dia."))
                .when(weeklyScheduleService)
                .validateWorkingDay(BARBER_ID, DayOfWeek.TUESDAY);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createAt(dateTime)
        );

        assertEquals("Barbeiro não atende neste dia.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void sundayShouldBeRejectedBecauseBarberDoesNotWork() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 6, 9, 30);
        doThrow(new BusinessException("Barbeiro não atende neste dia."))
                .when(weeklyScheduleService)
                .validateWorkingDay(BARBER_ID, DayOfWeek.SUNDAY);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createAt(dateTime)
        );

        assertEquals("Barbeiro não atende neste dia.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void wednesdayAtValidTimeShouldBeAllowed() {
        assertCreateAllowed(LocalDateTime.of(2026, 9, 2, 9, 30));
    }

    @Test
    void occupiedTimeShouldBeRejected() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(dateTime.minusHours(8)),
                eq(dateTime.plusMinutes(30))
        )).thenReturn(List.of(existingAppointment(dateTime, 40, AppointmentStatus.SCHEDULED)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createAt(dateTime)
        );

        assertEquals("Horário indisponível.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void canceledAppointmentShouldNotBlockSameTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(dateTime.minusHours(8)),
                eq(dateTime.plusMinutes(30))
        )).thenReturn(List.of(existingAppointment(dateTime, 40, AppointmentStatus.CANCELED)));

        assertDoesNotThrow(() -> createAt(dateTime));
    }

    @Test
    void updateShouldIgnoreOwnAppointmentWhenCheckingConflicts() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        AppointmentEntity appointment =
                existingAppointment(
                        dateTime,
                        40,
                        AppointmentStatus.SCHEDULED
                );
        ReflectionTestUtils.setField(appointment, "id", APPOINTMENT_ID);

        when(appointmentRepository.findByIdAndBarberId(
                APPOINTMENT_ID,
                BARBER_ID
        )).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(dateTime.minusHours(8)),
                eq(dateTime.plusMinutes(30))
        )).thenReturn(List.of(appointment));

        assertDoesNotThrow(
                () -> appointmentService.update(
                        BARBER_ID,
                        APPOINTMENT_ID,
                        new UpdateAppointmentRequest(
                                CUSTOMER_ID,
                                List.of(SERVICE_ID),
                                dateTime,
                                "teste"
                        )
                )
        );
    }

    @Test
    void scheduleBlockConflictShouldBeRejected() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        doThrow(new BusinessException("O horário do serviço está bloqueado."))
                .when(scheduleBlockService)
                .validateIntervalNotBlocked(BARBER_ID, dateTime, 30);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createAt(dateTime)
        );

        assertEquals("O horário do serviço está bloqueado.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void timeOutsideScheduleBlockShouldBeAllowed() {
        assertCreateAllowed(LocalDateTime.of(2026, 9, 2, 11, 0));
    }

    @Test
    void publicCreateShouldReturnCancelToken() {
        LocalDateTime dateTime =
                LocalDateTime.of(2026, 9, 2, 11, 0);

        assertNotNull(
                appointmentService.createPublic(
                                BARBER_ID,
                                new CreateAppointmentRequest(
                                        CUSTOMER_ID,
                                        List.of(SERVICE_ID),
                                        dateTime,
                                        "teste"
                                )
                        )
                        .getCancelToken()
        );
    }

    @Test
    void multipleServicesShouldSumPricesWithoutChangingWednesdayInterval() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 11, 0);

        var response = appointmentService.create(
                BARBER_ID,
                new CreateAppointmentRequest(
                        CUSTOMER_ID,
                        List.of(SERVICE_ID, SECOND_SERVICE_ID),
                        dateTime,
                        "teste"
                )
        );

        assertEquals(List.of(SERVICE_ID, SECOND_SERVICE_ID), response.getServiceIds());
        assertEquals(BigDecimal.valueOf(80), response.getTotalPrice());
        verify(scheduleBlockService)
                .validateIntervalNotBlocked(BARBER_ID, dateTime, 30);
    }

    @Test
    void cancelScheduledAppointmentShouldCreateAvailableSlot() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        AppointmentEntity appointment =
                existingAppointment(
                        dateTime,
                        40,
                        AppointmentStatus.SCHEDULED
                );

        when(appointmentRepository.findByCancelToken(
                APPOINTMENT_ID.toString()
        )).thenReturn(Optional.of(appointment));
        when(availableSlotService.registerAvailableSlot(barber, dateTime))
                .thenReturn(new com.barbersaas.availableslot.entity.AvailableSlotEntity(
                        barber, dateTime));

        appointmentService.cancelByToken(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.CANCELED,
                appointment.getStatus());
        verify(appointmentRepository).save(appointment);
        verify(availableSlotService).registerAvailableSlot(
                barber,
                dateTime
        );
        verify(eventPublisher).publishEvent(
                new com.barbersaas.appointments.event.AvailableSlotCreatedEvent(
                        barber.getId(), dateTime
                )
        );
    }

    @Test
    void cancelAlreadyCanceledAppointmentShouldNotCreateAvailableSlot() {
        AppointmentEntity appointment =
                existingAppointment(
                        LocalDateTime.of(2026, 9, 2, 10, 0),
                        40,
                        AppointmentStatus.CANCELED
                );

        when(appointmentRepository.findByCancelToken(
                APPOINTMENT_ID.toString()
        )).thenReturn(Optional.of(appointment));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> appointmentService.cancelByToken(APPOINTMENT_ID)
                );

        assertEquals("Agendamento já está cancelado.",
                exception.getMessage());
        verify(appointmentRepository, never()).save(appointment);
        verify(availableSlotService, never()).registerAvailableSlot(
                any(),
                any()
        );
    }

    @Test
    void mondayAvailableSlotsShouldMatchOfficialSchedule() {
        LocalDate monday = LocalDate.of(2026, 8, 31);
        when(weeklyScheduleService.getWorkingSchedule(BARBER_ID, DayOfWeek.MONDAY))
                .thenReturn(workingSchedule(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 30),
                        LocalTime.of(20, 0)
                ));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(monday.atStartOfDay()),
                eq(monday.atTime(LocalTime.MAX))
        )).thenReturn(List.of());
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, monday))
                .thenReturn(List.of());

        assertEquals(
                List.of(
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 10),
                        LocalTime.of(10, 50),
                        LocalTime.of(11, 30),
                        LocalTime.of(14, 0),
                        LocalTime.of(14, 40),
                        LocalTime.of(15, 20),
                        LocalTime.of(16, 0),
                        LocalTime.of(16, 40),
                        LocalTime.of(17, 20),
                        LocalTime.of(18, 0),
                        LocalTime.of(18, 40),
                        LocalTime.of(19, 20),
                        LocalTime.of(20, 0)
                ),
                appointmentService.getAvailableSlots(BARBER_ID, monday)
        );
    }

    @Test
    void wednesdayToSaturdayAvailableSlotsShouldUseThirtyMinuteIntervalsAndEndAt1930() {
        for (LocalDate date : List.of(
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2026, 9, 3),
                LocalDate.of(2026, 9, 4),
                LocalDate.of(2026, 9, 5)
        )) {
            when(weeklyScheduleService.getWorkingSchedule(BARBER_ID, date.getDayOfWeek()))
                    .thenReturn(workingSchedule(
                            date.getDayOfWeek(),
                            LocalTime.of(9, 30),
                            LocalTime.of(19, 30)
                    ));
            when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                    eq(BARBER_ID),
                    eq(date.atStartOfDay()),
                    eq(date.atTime(LocalTime.MAX))
            )).thenReturn(List.of());
            when(scheduleBlockService.findBlocksByDate(BARBER_ID, date))
                    .thenReturn(List.of());

            assertEquals(
                    List.of(
                            LocalTime.of(9, 30),
                            LocalTime.of(10, 0),
                            LocalTime.of(10, 30),
                            LocalTime.of(11, 0),
                            LocalTime.of(11, 30),
                            LocalTime.of(14, 0),
                            LocalTime.of(14, 30),
                            LocalTime.of(15, 0),
                            LocalTime.of(15, 30),
                            LocalTime.of(16, 0),
                            LocalTime.of(16, 30),
                            LocalTime.of(17, 0),
                            LocalTime.of(17, 30),
                            LocalTime.of(18, 0),
                            LocalTime.of(18, 30),
                            LocalTime.of(19, 0),
                            LocalTime.of(19, 30)
                    ),
                    appointmentService.getAvailableSlots(BARBER_ID, date)
            );
        }
    }

    @Test
    void tuesdayAvailableSlotsShouldRespectClosedDay() {
        LocalDate tuesday = LocalDate.of(2026, 9, 1);
        when(weeklyScheduleService.getWorkingSchedule(BARBER_ID, DayOfWeek.TUESDAY))
                .thenThrow(new BusinessException("Barbeiro não atende neste dia."));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> appointmentService.getAvailableSlots(BARBER_ID, tuesday)
        );

        assertEquals("Barbeiro não atende neste dia.", exception.getMessage());
    }

    @Test
    void availableSlotsShouldRemoveOccupiedTime() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        LocalDateTime occupied = wednesday.atTime(10, 0);
        when(weeklyScheduleService.getWorkingSchedule(BARBER_ID, DayOfWeek.WEDNESDAY))
                .thenReturn(workingSchedule(
                        DayOfWeek.WEDNESDAY,
                        LocalTime.of(9, 30),
                        LocalTime.of(19, 30)
                ));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of(existingAppointment(occupied, 30, AppointmentStatus.SCHEDULED)));
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of());

        List<LocalTime> slots = appointmentService.getAvailableSlots(BARBER_ID, wednesday);

        org.junit.jupiter.api.Assertions.assertFalse(slots.contains(LocalTime.of(10, 0)));
    }

    @Test
    void availableSlotsShouldRemoveBlockedTime() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        when(weeklyScheduleService.getWorkingSchedule(BARBER_ID, DayOfWeek.WEDNESDAY))
                .thenReturn(workingSchedule(
                        DayOfWeek.WEDNESDAY,
                        LocalTime.of(9, 30),
                        LocalTime.of(19, 30)
                ));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of());
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of(scheduleBlock(
                        wednesday.atTime(10, 0),
                        wednesday.atTime(10, 30)
                )));

        List<LocalTime> slots = appointmentService.getAvailableSlots(BARBER_ID, wednesday);

        org.junit.jupiter.api.Assertions.assertFalse(slots.contains(LocalTime.of(10, 0)));
    }

    @Test
    void dailyAgendaShouldReturnFreeAndOccupiedSlots() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        AppointmentEntity appointment =
                existingAppointment(
                        wednesday.atTime(10, 0),
                        AppointmentStatus.SCHEDULED,
                        List.of(service40Minutes),
                        BigDecimal.valueOf(50)
                );
        ReflectionTestUtils.setField(appointment, "id", APPOINTMENT_ID);

        mockWorkingDay(wednesday, LocalTime.of(9, 30), LocalTime.of(10, 30));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of(appointment));
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of());

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, wednesday);

        assertTrue(response.isWorkingDay());
        assertEquals(5, response.getSlots().size());
        assertEquals(DailyAgendaSlotStatus.FREE, response.getSlots().get(0).getStatus());
        assertEquals(DailyAgendaSlotStatus.OCCUPIED, response.getSlots().get(1).getStatus());
        assertEquals(APPOINTMENT_ID, response.getSlots().get(1).getAppointmentId());
        assertEquals(CUSTOMER_ID, response.getSlots().get(1).getCustomer().getId());
        assertEquals("Cliente", response.getSlots().get(1).getCustomer().getName());
    }

    @Test
    void dailyAgendaShouldReturnBlockedSlotFromScheduleBlock() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        ScheduleBlockEntity block =
                scheduleBlock(
                        wednesday.atTime(10, 0),
                        wednesday.atTime(10, 30)
                );
        UUID blockId =
                UUID.fromString("00000000-0000-0000-0000-000000000006");
        ReflectionTestUtils.setField(block, "id", blockId);

        mockWorkingDay(wednesday, LocalTime.of(9, 30), LocalTime.of(10, 30));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of());
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of(block));

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, wednesday);

        assertEquals(DailyAgendaSlotStatus.BLOCKED, response.getSlots().get(1).getStatus());
        assertEquals(blockId, response.getSlots().get(1).getBlock().getId());
        assertEquals("Bloqueio", response.getSlots().get(1).getBlock().getReason());
    }

    @Test
    void dailyAgendaShouldNotOccupyCanceledAppointmentSlot() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        AppointmentEntity appointment =
                existingAppointment(
                        wednesday.atTime(10, 0),
                        AppointmentStatus.CANCELED,
                        List.of(service40Minutes),
                        BigDecimal.valueOf(50)
                );

        mockWorkingDay(wednesday, LocalTime.of(9, 30), LocalTime.of(10, 30));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of(appointment));
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of());

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, wednesday);

        assertEquals(DailyAgendaSlotStatus.FREE, response.getSlots().get(1).getStatus());
        assertNull(response.getSlots().get(1).getAppointmentId());
    }

    @Test
    void dailyAgendaShouldReturnMultipleServicesAndTotalPrice() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        AppointmentEntity appointment =
                existingAppointment(
                        wednesday.atTime(9, 30),
                        AppointmentStatus.SCHEDULED,
                        List.of(service40Minutes, service30Minutes),
                        BigDecimal.valueOf(80)
                );

        mockWorkingDay(wednesday, LocalTime.of(9, 30), LocalTime.of(9, 30));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of(appointment));
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of());

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, wednesday);

        assertEquals(2, response.getSlots().get(0).getServices().size());
        assertEquals(SERVICE_ID, response.getSlots().get(0).getServices().get(0).getId());
        assertEquals(SECOND_SERVICE_ID, response.getSlots().get(0).getServices().get(1).getId());
        assertEquals(BigDecimal.valueOf(80), response.getSlots().get(0).getTotalPrice());
    }

    @Test
    void dailyAgendaShouldKeepMondayFortyMinuteCadence() {
        LocalDate monday = LocalDate.of(2026, 8, 31);
        mockWorkingDay(monday, LocalTime.of(9, 30), LocalTime.of(10, 50));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(monday.atStartOfDay()),
                eq(monday.atTime(LocalTime.MAX))
        )).thenReturn(List.of());
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, monday))
                .thenReturn(List.of());

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, monday);

        assertEquals(
                List.of(
                        monday.atTime(9, 30),
                        monday.atTime(10, 10),
                        monday.atTime(10, 50),
                        monday.atTime(11, 30)
                ),
                response.getSlots().stream()
                        .map(slot -> slot.getDateTime())
                        .toList()
        );
    }

    @Test
    void dailyAgendaShouldKeepOtherWorkingDaysThirtyMinuteCadence() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        mockWorkingDay(wednesday, LocalTime.of(9, 30), LocalTime.of(10, 30));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of());
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of());

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, wednesday);

        assertEquals(
                List.of(
                        wednesday.atTime(9, 30),
                        wednesday.atTime(10, 0),
                        wednesday.atTime(10, 30),
                        wednesday.atTime(11, 0),
                        wednesday.atTime(11, 30)
                ),
                response.getSlots().stream()
                        .map(slot -> slot.getDateTime())
                        .toList()
        );
    }

    @Test
    void dailyAgendaShouldNotReturnLunchSlots() {
        LocalDate wednesday = LocalDate.of(2026, 9, 2);
        mockWorkingDay(wednesday, LocalTime.of(11, 30), LocalTime.of(14, 30));
        when(appointmentRepository.findByBarberIdAndAppointmentDateTimeBetween(
                eq(BARBER_ID),
                eq(wednesday.atStartOfDay()),
                eq(wednesday.atTime(LocalTime.MAX))
        )).thenReturn(List.of());
        when(scheduleBlockService.findBlocksByDate(BARBER_ID, wednesday))
                .thenReturn(List.of());

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, wednesday);

        assertEquals(
                List.of(
                        wednesday.atTime(11, 30),
                        wednesday.atTime(14, 0),
                        wednesday.atTime(14, 30)
                ),
                response.getSlots().stream()
                        .map(slot -> slot.getDateTime())
                        .toList()
        );
    }

    @Test
    void dailyAgendaShouldReturnEmptySlotsForNonWorkingDay() {
        LocalDate tuesday = LocalDate.of(2026, 9, 1);
        WeeklyScheduleEntity schedule =
                workingSchedule(
                        DayOfWeek.TUESDAY,
                        null,
                        null
                );
        schedule.setWorkingDay(false);
        when(weeklyScheduleService.getSchedule(BARBER_ID, DayOfWeek.TUESDAY))
                .thenReturn(schedule);

        DailyAgendaResponse response =
                appointmentService.getDailyAgenda(BARBER_ID, tuesday);

        assertFalse(response.isWorkingDay());
        assertEquals(List.of(), response.getSlots());
        verify(appointmentRepository, never())
                .findByBarberIdAndAppointmentDateTimeBetween(any(), any(), any());
    }

    private void assertCreateAllowed(LocalDateTime dateTime) {
        assertDoesNotThrow(() -> createAt(dateTime));
    }

    private void createAt(LocalDateTime dateTime) {
        appointmentService.create(
                BARBER_ID,
                new CreateAppointmentRequest(
                        CUSTOMER_ID,
                        List.of(SERVICE_ID),
                        dateTime,
                        "teste"
                )
        );
    }

    private BarberEntity barber() {
        BarberEntity entity = new BarberEntity(
                "Barbeiro",
                "barbeiro@example.com",
                "(11) 99999-9999",
                "Corte",
                true
        );
        ReflectionTestUtils.setField(entity, "id", BARBER_ID);
        entity.setBarbershop(new BarbershopEntity(BARBERSHOP_ID, "Jhow Cortes", true));
        return entity;
    }

    private CustomerEntity customer() {
        CustomerEntity entity = new CustomerEntity(
                "Cliente",
                "(11) 98888-8888",
                "cliente@example.com",
                null,
                null,
                true
        );
        ReflectionTestUtils.setField(entity, "id", CUSTOMER_ID);
        entity.setBarbershop(new BarbershopEntity(BARBERSHOP_ID, "Jhow Cortes", true));
        return entity;
    }

    private ServiceEntity service(UUID id, int durationMinutes, int price) {
        ServiceEntity entity = new ServiceEntity(
                "Corte",
                "Corte masculino",
                durationMinutes,
                BigDecimal.valueOf(price),
                true
        );
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setBarbershop(new BarbershopEntity(BARBERSHOP_ID, "Jhow Cortes", true));
        return entity;
    }

    private AppointmentEntity existingAppointment(
            LocalDateTime dateTime,
            int durationMinutes,
            AppointmentStatus status) {

        return new AppointmentEntity(
                customer,
                barber,
                List.of(service(SERVICE_ID, durationMinutes, 50)),
                BigDecimal.valueOf(50),
                dateTime,
                status,
                null
        );
    }

    private WeeklyScheduleEntity workingSchedule(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime) {

        WeeklyScheduleEntity schedule = new WeeklyScheduleEntity(
                null,
                dayOfWeek,
                startTime,
                endTime,
                true
        );
        schedule.setBreakStartTime(LocalTime.of(12, 0));
        schedule.setBreakEndTime(LocalTime.of(14, 0));
        return schedule;
    }

    private ScheduleBlockEntity scheduleBlock(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        return new ScheduleBlockEntity(
                null,
                startDateTime,
                endDateTime,
                "Bloqueio"
        );
    }

    private void mockWorkingDay(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {

        when(weeklyScheduleService.getSchedule(BARBER_ID, date.getDayOfWeek()))
                .thenReturn(workingSchedule(
                        date.getDayOfWeek(),
                        startTime,
                        endTime
                ));
    }

    private AppointmentEntity existingAppointment(
            LocalDateTime dateTime,
            AppointmentStatus status,
            List<ServiceEntity> services,
            BigDecimal totalPrice) {

        return new AppointmentEntity(
                customer,
                barber,
                services,
                totalPrice,
                dateTime,
                status,
                null
        );
    }
}
