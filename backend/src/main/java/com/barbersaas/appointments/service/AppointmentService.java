package com.barbersaas.appointments.service;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.dto.CreateAppointmentRequest;
import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.appointments.dto.UpdateAppointmentRequest;
import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.mapper.AppointmentMapper;
import com.barbersaas.appointments.repository.AppointmentRepository;
import com.barbersaas.availableslot.service.AvailableSlotService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository,
            AppointmentMapper appointmentMapper,
            ScheduleBlockService scheduleBlockService,
            WeeklyScheduleService weeklyScheduleService,
            AvailableSlotService availableSlotService) {

        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentMapper = appointmentMapper;
        this.scheduleBlockService = scheduleBlockService;
        this.weeklyScheduleService = weeklyScheduleService;
        this.availableSlotService = availableSlotService;
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
                generateTimeSlots(
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        intervalMinutes,
                        schedule.getBreakStartTime(),
                        schedule.getBreakEndTime()
                );

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

    private List<LocalTime> generateTimeSlots(
            LocalTime startTime,
            LocalTime endTime,
            int intervalMinutes,
            LocalTime breakStartTime,
            LocalTime breakEndTime) {

        List<LocalTime> slots =
                new ArrayList<>();

        /*
         * Período antes do almoço.
         *
         * Exemplo de segunda-feira:
         * 09:30
         * 10:10
         * 10:50
         * 11:30
         */
        LocalTime current =
                startTime;

        if (breakStartTime != null) {

            while (current.isBefore(
                    breakStartTime)) {

                slots.add(current);

                current = current.plusMinutes(
                        intervalMinutes
                );
            }

        } else {

            while (!current.isAfter(endTime)) {

                slots.add(current);

                current = current.plusMinutes(
                        intervalMinutes
                );
            }

            return slots;
        }

        /*
         * Período depois do almoço.
         *
         * IMPORTANTE:
         * A contagem recomeça exatamente
         * no horário de retorno do almoço.
         *
         * Segunda-feira:
         * 14:00
         * 14:40
         * 15:20
         * 16:00
         * 16:40
         * 17:20
         * 18:00
         * 18:40
         * 19:20
         * 20:00
         */
        current =
                breakEndTime;

        while (!current.isAfter(endTime)) {

            slots.add(current);

            current = current.plusMinutes(
                    intervalMinutes
            );
        }

        return slots;
    }

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

        return appointmentRepository.save(entity);
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

        AppointmentEntity updatedEntity =
                appointmentRepository.save(
                        appointment
                );

        return appointmentMapper.toResponse(
                updatedEntity
        );
    }

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

        appointmentRepository.delete(
                appointment
        );
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

        if (appointment.getStatus()
                == AppointmentStatus.CANCELED) {

            throw new BusinessException(
                    "Agendamento já está cancelado."
            );
        }

        BarberEntity barber =
                appointment.getBarber();

        LocalDateTime canceledDateTime =
                appointment.getAppointmentDateTime();

        appointment.setStatus(
                AppointmentStatus.CANCELED
        );

        appointmentRepository.save(
                appointment
        );

        availableSlotService.registerAvailableSlot(
                barber,
                canceledDateTime
        );
    }
}
