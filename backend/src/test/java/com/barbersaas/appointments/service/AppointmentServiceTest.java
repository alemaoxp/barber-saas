package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.mapper.AppointmentMapper;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SERVICE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");

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

    private AppointmentService appointmentService;
    private BarberEntity barber;
    private CustomerEntity customer;
    private ServiceEntity service40Minutes;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                customerRepository,
                barberRepository,
                serviceRepository,
                new AppointmentMapper(),
                scheduleBlockService,
                weeklyScheduleService
        );

        barber = barber();
        customer = customer();
        service40Minutes = service(40);

        lenient().when(barberRepository.findById(BARBER_ID))
                .thenReturn(Optional.of(barber));
        lenient().when(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customer));
        lenient().when(serviceRepository.findById(SERVICE_ID))
                .thenReturn(Optional.of(service40Minutes));
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
                eq(dateTime.plusMinutes(40))
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
                eq(dateTime.plusMinutes(40))
        )).thenReturn(List.of(existingAppointment(dateTime, 40, AppointmentStatus.CANCELED)));

        assertDoesNotThrow(() -> createAt(dateTime));
    }

    @Test
    void scheduleBlockConflictShouldBeRejected() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        doThrow(new BusinessException("O horário do serviço está bloqueado."))
                .when(scheduleBlockService)
                .validateIntervalNotBlocked(BARBER_ID, dateTime, 40);

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

    private void assertCreateAllowed(LocalDateTime dateTime) {
        assertDoesNotThrow(() -> createAt(dateTime));
    }

    private void createAt(LocalDateTime dateTime) {
        appointmentService.create(
                BARBER_ID,
                new CreateAppointmentRequest(
                        CUSTOMER_ID,
                        SERVICE_ID,
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
        return entity;
    }

    private ServiceEntity service(int durationMinutes) {
        ServiceEntity entity = new ServiceEntity(
                "Corte",
                "Corte masculino",
                durationMinutes,
                BigDecimal.valueOf(50),
                true
        );
        ReflectionTestUtils.setField(entity, "id", SERVICE_ID);
        return entity;
    }

    private AppointmentEntity existingAppointment(
            LocalDateTime dateTime,
            int durationMinutes,
            AppointmentStatus status) {

        return new AppointmentEntity(
                customer,
                barber,
                service(durationMinutes),
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
}
