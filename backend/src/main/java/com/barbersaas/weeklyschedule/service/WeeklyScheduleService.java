package com.barbersaas.weeklyschedule.service;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.NotFoundException;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleDayDto;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleRequest;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleResponse;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.mapper.WeeklyScheduleMapper;
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final WeeklyScheduleMapper weeklyScheduleMapper;
    private final BarberScheduleRepository barberScheduleRepository;

    public WeeklyScheduleService(
            WeeklyScheduleRepository weeklyScheduleRepository,
            WeeklyScheduleMapper weeklyScheduleMapper,
            BarberScheduleRepository barberScheduleRepository) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.weeklyScheduleMapper = weeklyScheduleMapper;
        this.barberScheduleRepository = barberScheduleRepository;
    }

    public WeeklyScheduleResponse getWeeklySchedule(UUID barberId) {
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);

        List<WeeklyScheduleEntity> schedules =
                weeklyScheduleRepository.findByBarberScheduleIdOrderByDayOfWeek(
                        barberSchedule.getId()
                );

        return weeklyScheduleMapper.toResponse(schedules);
    }

    public WeeklyScheduleResponse updateWeeklySchedule(
            UUID barberId,
            WeeklyScheduleRequest request) {

        validateRequest(request);

        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);

        Map<DayOfWeek, WeeklyScheduleEntity> existingSchedules =
                findWeeklyScheduleMap(barberSchedule.getId());

        updateSchedules(existingSchedules, request);

        return weeklyScheduleMapper.toResponse(
                new ArrayList<>(existingSchedules.values())
        );
    }

    public void validateWorkingDay(
            UUID barberId,
            DayOfWeek dayOfWeek) {

        BarberScheduleEntity barberSchedule =
                findBarberSchedule(barberId);

        WeeklyScheduleEntity schedule =
                findWeeklyScheduleForDay(
                        barberSchedule.getId(),
                        dayOfWeek
                );

        if (!schedule.isWorkingDay()) {
            throw new BusinessException(
                    "Barbeiro não atende neste dia."
            );
        }
    }

    public void validateWorkingHours(
            UUID barberId,
            LocalDateTime appointmentDateTime) {

        validateWorkingDay(
                barberId,
                appointmentDateTime.getDayOfWeek()
        );

        BarberScheduleEntity barberSchedule =
                findBarberSchedule(barberId);

        WeeklyScheduleEntity schedule =
                findWeeklyScheduleForDay(
                        barberSchedule.getId(),
                        appointmentDateTime.getDayOfWeek()
                );

        LocalTime appointmentTime =
                appointmentDateTime.toLocalTime();

        if (appointmentTime.isBefore(schedule.getStartTime())
                || appointmentTime.isAfter(schedule.getEndTime())) {

            throw new BusinessException(
                    "Horário fora do expediente."
            );
        }

        if (schedule.getBreakStartTime() != null
                && schedule.getBreakEndTime() != null
                && !appointmentTime.isBefore(schedule.getBreakStartTime())
                && appointmentTime.isBefore(schedule.getBreakEndTime())) {

            throw new BusinessException(
                    "Horário dentro do intervalo."
            );
        }
    }

    public WeeklyScheduleEntity getWorkingSchedule(
            UUID barberId,
            DayOfWeek dayOfWeek) {

        validateWorkingDay(barberId, dayOfWeek);

        BarberScheduleEntity barberSchedule =
                findBarberSchedule(barberId);

        return findWeeklyScheduleForDay(
                barberSchedule.getId(),
                dayOfWeek
        );
    }

    public WeeklyScheduleEntity getSchedule(
            UUID barberId,
            DayOfWeek dayOfWeek) {

        BarberScheduleEntity barberSchedule =
                findBarberSchedule(barberId);

        return findWeeklyScheduleForDay(
                barberSchedule.getId(),
                dayOfWeek
        );
    }

    private BarberScheduleEntity findBarberSchedule(UUID barberId) {
        return barberScheduleRepository.findByBarberId(barberId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Configuração de agenda não encontrada."
                        )
                );
    }

    private WeeklyScheduleEntity findWeeklyScheduleForDay(
            UUID barberScheduleId,
            DayOfWeek dayOfWeek) {

        return weeklyScheduleRepository
                .findByBarberScheduleIdAndDayOfWeek(
                        barberScheduleId,
                        dayOfWeek
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Agenda semanal não encontrada para o dia: "
                                        + dayOfWeek
                        )
                );
    }

    private Map<DayOfWeek, WeeklyScheduleEntity> findWeeklyScheduleMap(
            UUID barberScheduleId) {

        List<WeeklyScheduleEntity> schedules =
                weeklyScheduleRepository
                        .findByBarberScheduleIdOrderByDayOfWeek(
                                barberScheduleId
                        );

        Map<DayOfWeek, WeeklyScheduleEntity> scheduleMap =
                new HashMap<>();

        for (WeeklyScheduleEntity entity : schedules) {
            scheduleMap.put(
                    entity.getDayOfWeek(),
                    entity
            );
        }

        return scheduleMap;
    }

    private void validateRequest(
            WeeklyScheduleRequest request) {

        if (request.getWeeklySchedule() == null) {
            throw new BusinessException(
                    "A agenda semanal é obrigatória."
            );
        }

        List<WeeklyScheduleDayDto> weeklySchedule =
                request.getWeeklySchedule();

        if (weeklySchedule.size() != 7) {
            throw new BusinessException(
                    "A agenda semanal deve conter exatamente 7 dias."
            );
        }

        Set<DayOfWeek> daysOfWeek = new HashSet<>();

        for (WeeklyScheduleDayDto dto : weeklySchedule) {

            if (!daysOfWeek.add(dto.getDayOfWeek())) {
                throw new BusinessException(
                        "Existem dias da semana duplicados."
                );
            }

            if (!dto.isWorkingDay()) {

                if (dto.getStartTime() != null
                        || dto.getEndTime() != null
                        || dto.getBreakStartTime() != null
                        || dto.getBreakEndTime() != null) {

                    throw new BusinessException(
                            "Dias não trabalhados não devem possuir horários."
                    );
                }

                continue;
            }

            if (dto.getStartTime() == null
                    || dto.getEndTime() == null) {

                throw new BusinessException(
                        "Dias trabalhados devem possuir horário inicial e final."
                );
            }

            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                throw new BusinessException(
                        "O horário inicial deve ser anterior ao horário final."
                );
            }

            boolean hasBreakStart =
                    dto.getBreakStartTime() != null;

            boolean hasBreakEnd =
                    dto.getBreakEndTime() != null;

            if (hasBreakStart != hasBreakEnd) {
                throw new BusinessException(
                        "O intervalo deve possuir horário inicial e final."
                );
            }

            if (hasBreakStart) {

                if (!dto.getBreakStartTime()
                        .isBefore(dto.getBreakEndTime())) {

                    throw new BusinessException(
                            "O início do intervalo deve ser anterior ao fim do intervalo."
                    );
                }

                if (dto.getBreakStartTime()
                        .isBefore(dto.getStartTime())
                        || dto.getBreakEndTime()
                        .isAfter(dto.getEndTime())) {

                    throw new BusinessException(
                            "O intervalo deve estar dentro do horário de expediente."
                    );
                }
            }
        }
    }

    private void updateSchedules(
            Map<DayOfWeek, WeeklyScheduleEntity> existingSchedules,
            WeeklyScheduleRequest request) {

        for (WeeklyScheduleDayDto dto :
                request.getWeeklySchedule()) {

            WeeklyScheduleEntity entity =
                    existingSchedules.get(dto.getDayOfWeek());

            if (entity == null) {
                throw new BusinessException(
                        "Agenda semanal inconsistente para o dia: "
                                + dto.getDayOfWeek()
                );
            }

            weeklyScheduleMapper.updateEntity(
                    entity,
                    dto
            );
        }

        weeklyScheduleRepository.saveAll(
                existingSchedules.values()
        );
    }
}
