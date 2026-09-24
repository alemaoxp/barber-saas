package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.DailyAgendaBlockSummary;
import com.barbersaas.appointments.dto.DailyAgendaCustomerSummary;
import com.barbersaas.appointments.dto.DailyAgendaResponse;
import com.barbersaas.appointments.dto.DailyAgendaServiceSummary;
import com.barbersaas.appointments.dto.DailyAgendaSlotResponse;
import com.barbersaas.appointments.dto.DashboardSummaryResponse;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.event.AvailableSlotCreatedEvent;
import com.barbersaas.appointments.mapper.AppointmentMapper;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.availableslot.service.AvailableSlotService;
import com.barbersaas.availableslot.entity.AvailableSlotEntity;
import com.barbersaas.availableslot.enums.AvailableSlotStatus;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import com.barbersaas.scheduleblock.entity.ScheduleBlockEntity;
import com.barbersaas.scheduleblock.service.ScheduleBlockService;
import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.repository.ServiceRepository;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.service.WeeklyScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentMapper appointmentMapper;
    private final ScheduleBlockService scheduleBlockService;
    private final WeeklyScheduleService weeklyScheduleService;
    private final AvailableSlotService availableSlotService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository,
            AppointmentMapper appointmentMapper,
            ScheduleBlockService scheduleBlockService,
            WeeklyScheduleService weeklyScheduleService,
            AvailableSlotService availableSlotService,
            ApplicationEventPublisher eventPublisher) {

        this(
                appointmentRepository,
                customerRepository,
                barberRepository,
                serviceRepository,
                appointmentMapper,
                scheduleBlockService,
                weeklyScheduleService,
                availableSlotService,
                eventPublisher,
                Clock.systemDefaultZone()
        );
    }

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository,
            AppointmentMapper appointmentMapper,
            ScheduleBlockService scheduleBlockService,
            WeeklyScheduleService weeklyScheduleService,
            AvailableSlotService availableSlotService,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {

        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentMapper = appointmentMapper;
        this.scheduleBlockService = scheduleBlockService;
        this.weeklyScheduleService = weeklyScheduleService;
        this.availableSlotService = availableSlotService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    private BarberEntity findBarber(UUID barberId) {
        return barberRepository.findById(barberId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Barbeiro não encontrado."
                        ));
    }

    private AppointmentEntity findAppointment(
            UUID appointmentId,
            UUID barberId) {

        return appointmentRepository
                .findByIdAndBarberId(appointmentId, barberId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Agendamento não encontrado."
                        ));
    }

    public DashboardSummaryResponse getDashboardSummary(
            UUID barberId,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("A data inicial não pode ser posterior à data final.");
        }

        findBarber(barberId);
        List<AppointmentEntity> appointments = appointmentRepository
                .findByBarberIdAndAppointmentDateTimeBetween(
                        barberId,
                        startDate.atStartOfDay(),
                        endDate.atTime(LocalTime.MAX)
                )
                .stream()
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELED)
                .toList();

        BigDecimal scheduledValue = appointments.stream()
                .map(AppointmentEntity::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummaryResponse(
                barberId,
                startDate,
                endDate,
                appointments.size(),
                scheduledValue
        );
    }

    private void validateAvailability(
            UUID barberId,
            LocalDateTime appointmentDateTime) {

        validateAvailability(
                barberId,
                appointmentDateTime,
                null
        );
    }

    public void validateAvailabilityIgnoringAppointment(
            UUID barberId,
            LocalDateTime appointmentDateTime,
            UUID ignoredAppointmentId) {

        validateAvailability(
                barberId,
                appointmentDateTime,
                ignoredAppointmentId
        );
    }

    private void validateAvailability(
            UUID barberId,
            LocalDateTime appointmentDateTime,
            UUID ignoredAppointmentId) {

        validateBookingWindow(barberId, appointmentDateTime);

        int intervalMinutes = getAppointmentIntervalMinutes(
                appointmentDateTime.getDayOfWeek()
        );

        weeklyScheduleService.validateWorkingDay(
                barberId,
                appointmentDateTime.getDayOfWeek()
        );

        weeklyScheduleService.validateWorkingHours(
                barberId,
                appointmentDateTime
        );

        scheduleBlockService.validateIntervalNotBlocked(
                barberId,
                appointmentDateTime,
                intervalMinutes
        );

        LocalDateTime appointmentEnd =
                appointmentDateTime.plusMinutes(intervalMinutes);

        LocalDateTime searchStart =
                appointmentDateTime.minusHours(8);

        List<AppointmentEntity> appointments =
                appointmentRepository
                        .findByBarberIdAndAppointmentDateTimeBetween(
                                barberId,
                                searchStart,
                                appointmentEnd
                        );

        boolean hasConflict = appointments.stream()
                .anyMatch(existingAppointment -> {

                    if (existingAppointment.getId() != null
                            && existingAppointment.getId()
                            .equals(ignoredAppointmentId)) {
                        return false;
                    }

                    if (existingAppointment.getStatus()
                            == AppointmentStatus.CANCELED) {
                        return false;
                    }

                    LocalDateTime existingStart =
                            existingAppointment.getAppointmentDateTime();

                    int existingDuration = getAppointmentIntervalMinutes(
                            existingStart.getDayOfWeek()
                    );

                    LocalDateTime existingEnd =
                            existingStart.plusMinutes(existingDuration);

                    return existingStart.isBefore(appointmentEnd)
                            && existingEnd.isAfter(appointmentDateTime);
                });

        if (hasConflict) {
            throw new BusinessException(
                    "Horário indisponível."
            );
        }
    }

    private void validateBookingWindow(
            UUID barberId,
            LocalDateTime appointmentDateTime) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (!appointmentDateTime.isAfter(now)) {
            throw new BusinessException("O horário do agendamento deve estar no futuro.");
        }

        int maxBookingDays = weeklyScheduleService.getMaxBookingDays(barberId);
        if (appointmentDateTime.toLocalDate()
                .isAfter(now.toLocalDate().plusDays(maxBookingDays))) {
            throw new BusinessException("O agendamento excede a janela permitida.");
        }
    }

    public List<LocalTime> getAvailableSlots(
            UUID barberId,
            LocalDate date) {

        DayOfWeek dayOfWeek =
                date.getDayOfWeek();

        WeeklyScheduleEntity schedule =
                weeklyScheduleService.getWorkingSchedule(
                        barberId,
                        dayOfWeek
                );

        int intervalMinutes =
                getAppointmentIntervalMinutes(dayOfWeek);

        List<LocalTime> allSlots =
                WeeklyScheduleService.generateFixedSlots(schedule, intervalMinutes);

        LocalDateTime startOfDay =
                date.atStartOfDay();

        LocalDateTime endOfDay =
                date.atTime(LocalTime.MAX);

        List<AppointmentEntity> appointments =
                appointmentRepository
                        .findByBarberIdAndAppointmentDateTimeBetween(
                                barberId,
                                startOfDay,
                                endOfDay
                        );

        Set<LocalDateTime> bookedTimes =
                appointments.stream()
                        .filter(appointment ->
                                appointment.getStatus()
                                        != AppointmentStatus.CANCELED)
                        .map(AppointmentEntity::getAppointmentDateTime)
                        .collect(Collectors.toSet());

        List<ScheduleBlockEntity> blocks =
                scheduleBlockService.findBlocksByDate(
                        barberId,
                        date
                );

        Set<LocalDateTime> blockedTimes =
                new HashSet<>();

        for (ScheduleBlockEntity block : blocks) {

            LocalDateTime current =
                    block.getStartDateTime();

            while (current.isBefore(
                    block.getEndDateTime())) {

                blockedTimes.add(current);

                current = current.plusMinutes(
                        intervalMinutes
                );
            }
        }

        return allSlots.stream()
                .filter(slotTime -> {

                    LocalDateTime slotDateTime =
                            LocalDateTime.of(
                                    date,
                                    slotTime
                            );

                    if (bookedTimes.contains(slotDateTime)) {
                        return false;
                    }

                    if (blockedTimes.contains(slotDateTime)) {
                        return false;
                    }

                    if (isInsideBreak(
                            schedule,
                            slotTime)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public DailyAgendaResponse getDailyAgenda(
            UUID barberId,
            LocalDate date) {

        findBarber(barberId);

        WeeklyScheduleEntity schedule =
                weeklyScheduleService.getSchedule(
                        barberId,
                        date.getDayOfWeek()
                );

        if (!schedule.isWorkingDay()) {
            return new DailyAgendaResponse(
                    barberId,
                    date,
                    false,
                    List.of()
            );
        }

        int intervalMinutes =
                getAppointmentIntervalMinutes(
                        date.getDayOfWeek()
                );

        List<LocalTime> allSlots =
                WeeklyScheduleService.generateFixedSlots(schedule, intervalMinutes);

        LocalDateTime startOfDay =
                date.atStartOfDay();

        LocalDateTime endOfDay =
                date.atTime(LocalTime.MAX);

        Map<LocalDateTime, AppointmentEntity> appointmentsByDateTime =
                appointmentRepository
                        .findByBarberIdAndAppointmentDateTimeBetween(
                                barberId,
                                startOfDay,
                                endOfDay
                        )
                        .stream()
                        .filter(appointment ->
                                appointment.getStatus()
                                        != AppointmentStatus.CANCELED)
                        .collect(Collectors.toMap(
                                AppointmentEntity::getAppointmentDateTime,
                                appointment -> appointment,
                                (first, ignored) -> first
                        ));

        List<ScheduleBlockEntity> blocks =
                scheduleBlockService.findBlocksByDate(
                        barberId,
                        date
                );

        List<DailyAgendaSlotResponse> slots =
                allSlots.stream()
                        .map(slotTime -> dailyAgendaSlot(
                                date,
                                slotTime,
                                intervalMinutes,
                                appointmentsByDateTime,
                                blocks
                        ))
                        .toList();

        return new DailyAgendaResponse(
                barberId,
                date,
                true,
                slots
        );
    }

    public Optional<DailyAgendaSlotResponse> findNextScheduledAppointment(
            UUID barberId,
            LocalDateTime now) {

        findBarber(barberId);

        return appointmentRepository
                .findFirstByBarberIdAndStatusAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(
                        barberId,
                        AppointmentStatus.SCHEDULED,
                        now
                )
                .map(appointment -> dailyAgendaOccupiedSlot(
                        appointment.getAppointmentDateTime(),
                        appointment
                ));
    }

    private DailyAgendaSlotResponse dailyAgendaSlot(
            LocalDate date,
            LocalTime slotTime,
            int intervalMinutes,
            Map<LocalDateTime, AppointmentEntity> appointmentsByDateTime,
            List<ScheduleBlockEntity> blocks) {

        LocalDateTime slotDateTime =
                LocalDateTime.of(
                        date,
                        slotTime
                );

        AppointmentEntity appointment =
                appointmentsByDateTime.get(slotDateTime);

        if (appointment != null) {
            return dailyAgendaOccupiedSlot(
                    slotDateTime,
                    appointment
            );
        }

        Optional<ScheduleBlockEntity> block =
                blocks.stream()
                        .filter(candidate ->
                                overlaps(
                                        slotDateTime,
                                        slotDateTime.plusMinutes(intervalMinutes),
                                        candidate.getStartDateTime(),
                                        candidate.getEndDateTime()
                                ))
                        .findFirst();

        if (block.isPresent()) {
            return DailyAgendaSlotResponse.blocked(
                    slotDateTime,
                    new DailyAgendaBlockSummary(
                            block.get().getId(),
                            block.get().getStartDateTime(),
                            block.get().getEndDateTime(),
                            block.get().getReason()
                    )
            );
        }

        return DailyAgendaSlotResponse.free(slotDateTime);
    }

    private DailyAgendaSlotResponse dailyAgendaOccupiedSlot(
            LocalDateTime slotDateTime,
            AppointmentEntity appointment) {

        CustomerEntity customer =
                appointment.getCustomer();

        List<DailyAgendaServiceSummary> services =
                appointment.getServices()
                        .stream()
                        .map(service -> new DailyAgendaServiceSummary(
                                service.getId(),
                                service.getName()
                        ))
                        .toList();

        return DailyAgendaSlotResponse.occupied(
                slotDateTime,
                appointment.getId(),
                appointment.getStatus(),
                new DailyAgendaCustomerSummary(
                        customer.getId(),
                        customer.getName()
                ),
                services,
                appointment.getTotalPrice()
        );
    }

    private boolean overlaps(
            LocalDateTime firstStart,
            LocalDateTime firstEnd,
            LocalDateTime secondStart,
            LocalDateTime secondEnd) {

        return firstStart.isBefore(secondEnd)
                && firstEnd.isAfter(secondStart);
    }

    private boolean isInsideBreak(
            WeeklyScheduleEntity schedule,
            LocalTime slotTime) {

        if (schedule.getBreakStartTime() == null
                || schedule.getBreakEndTime() == null) {
            return false;
        }

        return !slotTime.isBefore(
                schedule.getBreakStartTime())
                && slotTime.isBefore(
                schedule.getBreakEndTime());
    }

    private int getAppointmentIntervalMinutes(
            DayOfWeek dayOfWeek) {

        if (dayOfWeek == DayOfWeek.MONDAY) {
            return 40;
        }

        return 30;
    }

    @Transactional
    public AppointmentResponse create(
            UUID barberId,
            CreateAppointmentRequest request) {

        return appointmentMapper.toResponse(
                createEntity(
                        barberId,
                        request,
                        false
                )
        );
    }

    @Transactional
    public PublicAppointmentResponse createPublic(
            UUID barberId,
            CreateAppointmentRequest request) {

        return appointmentMapper.toPublicResponse(
                createEntity(
                        barberId,
                        request,
                        true
                )
        );
    }

    private AppointmentEntity createEntity(
            UUID barberId,
            CreateAppointmentRequest request,
            boolean generateCancelToken) {

        BarberEntity barber =
                findBarber(barberId);

        CustomerEntity customer =
                customerRepository.findById(
                                request.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Cliente não encontrado."
                                ));

        List<ServiceEntity> services = findServices(request.getServiceIds());

        validateOwnership(barber, customer, services);

        validateAvailability(barberId, request.getAppointmentDateTime());

        AppointmentEntity entity =
                appointmentMapper.toEntity(
                        request,
                        customer,
                        barber,
                        services,
                        totalPrice(services)
                );

        if (generateCancelToken) {
            entity.setCancelToken(
                    UUID.randomUUID().toString()
            );
        }

        return saveScheduledAppointment(entity);
    }

    private List<ServiceEntity> findServices(List<UUID> serviceIds) {
        if (serviceIds.size() != new LinkedHashSet<>(serviceIds).size()) {
            throw new BusinessException("Serviços não podem se repetir.");
        }

        return serviceIds.stream()
                .map(serviceId -> serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new NotFoundException("Serviço não encontrado.")))
                .toList();
    }

    private void validateOwnership(BarberEntity barber, CustomerEntity customer, List<ServiceEntity> services) {
        UUID barbershopId = barber.getBarbershop().getId();
        if (!barbershopId.equals(customer.getBarbershop().getId())
                || services.stream().anyMatch(service -> !barbershopId.equals(service.getBarbershop().getId()))) {
            throw new BusinessException("Cliente e serviços devem pertencer à mesma barbearia do barbeiro.");
        }
    }

    private BigDecimal totalPrice(List<ServiceEntity> services) {
        return services.stream()
                .map(ServiceEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<AppointmentResponse> findAll(
            UUID barberId) {

        BarberEntity barber =
                findBarber(barberId);

        List<AppointmentEntity> appointments =
                appointmentRepository.findByBarberId(
                        barber.getId()
                );

        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PublicAppointmentResponse> findPublicByCustomerId(UUID customerId) {
        return appointmentRepository.findByCustomerIdOrderByAppointmentDateTimeDesc(customerId)
                .stream()
                .map(appointmentMapper::toPublicResponse)
                .toList();
    }

    public AppointmentResponse findById(
            UUID barberId,
            UUID appointmentId) {

        BarberEntity barber =
                findBarber(barberId);

        AppointmentEntity appointment =
                findAppointment(
                        appointmentId,
                        barber.getId()
                );

        return appointmentMapper.toResponse(
                appointment
        );
    }

    @Transactional
    public AppointmentResponse update(
            UUID barberId,
            UUID appointmentId,
            UpdateAppointmentRequest request) {

        BarberEntity barber =
                findBarber(barberId);

        AppointmentEntity appointment =
                findAppointment(
                        appointmentId,
                        barber.getId()
                );

        CustomerEntity customer =
                customerRepository.findById(
                                request.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Cliente não encontrado."
                                ));

        List<ServiceEntity> services = findServices(request.getServiceIds());

        validateOwnership(barber, customer, services);

        validateAvailabilityIgnoringAppointment(
                barberId,
                request.getAppointmentDateTime(),
                appointment.getId()
        );

        appointmentMapper.updateEntity(
                appointment,
                request,
                customer,
                services,
                totalPrice(services)
        );

        AppointmentEntity updatedEntity = saveScheduledAppointment(appointment);

        return appointmentMapper.toResponse(
                updatedEntity
        );
    }

    @Transactional
    public void delete(
            UUID barberId,
            UUID appointmentId) {

        BarberEntity barber =
                findBarber(barberId);

        AppointmentEntity appointment =
                findAppointment(
                        appointmentId,
                        barber.getId()
                );

        cancel(appointment);
    }

    public AppointmentResponse updateStatus(
            UUID barberId,
            UUID appointmentId,
            AppointmentStatus status) {

        BarberEntity barber = findBarber(barberId);
        AppointmentEntity appointment = findAppointment(appointmentId, barber.getId());

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException(
                    "Somente agendamentos pendentes podem ter o status alterado."
            );
        }

        if (status != AppointmentStatus.COMPLETED && status != AppointmentStatus.NO_SHOW) {
            throw new BusinessException("Status de atendimento inválido.");
        }

        appointment.setStatus(status);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentEntity saveScheduledAppointment(AppointmentEntity appointment) {
        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException exception) {
            if (isScheduledSlotConflict(exception)) {
                throw new BusinessException("Horário indisponível.", exception);
            }
            throw exception;
        }
    }

    private boolean isScheduledSlotConflict(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null
                    && current.getMessage().contains("ux_appointments_scheduled_slot")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Transactional
    public void cancelByToken(UUID token) {

        AppointmentEntity appointment =
                appointmentRepository
                        .findByCancelToken(
                                token.toString()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Agendamento não encontrado."
                                ));

        cancel(appointment);
    }

    private void cancel(AppointmentEntity appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BusinessException("Agendamento já está cancelado.");
        }

        BarberEntity barber = appointment.getBarber();
        LocalDateTime canceledDateTime = appointment.getAppointmentDateTime();
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);

        AvailableSlotEntity availableSlot = availableSlotService.registerAvailableSlot(
                barber, canceledDateTime);
        if (availableSlot.getStatus() == AvailableSlotStatus.AVAILABLE) {
            eventPublisher.publishEvent(new AvailableSlotCreatedEvent(
                    barber.getId(), canceledDateTime, availableSlot.getId()));
        }
    }
}
