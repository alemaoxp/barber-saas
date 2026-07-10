package com.barbersaas.weeklyschedule.service;

import com.barbersaas.barberschedules.entity.BarberScheduleEntity;
import com.barbersaas.barberschedules.repository.BarberScheduleRepository;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleRequest;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleResponse;
import com.barbersaas.weeklyschedule.entity.WeeklyScheduleEntity;
import com.barbersaas.weeklyschedule.mapper.WeeklyScheduleMapper;
import com.barbersaas.weeklyschedule.repository.WeeklyScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.barbersaas.weeklyschedule.dto.WeeklyScheduleDayDto;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


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
        List<WeeklyScheduleEntity> schedules = weeklyScheduleRepository.findByBarberScheduleIdOrderByDayOfWeek(barberSchedule.getId());
        return weeklyScheduleMapper.toResponse(schedules);
    }

    public WeeklyScheduleResponse updateWeeklySchedule(UUID barberId, WeeklyScheduleRequest request) {
        validateRequest(request);
        BarberScheduleEntity barberSchedule = findBarberSchedule(barberId);
        Map<DayOfWeek, WeeklyScheduleEntity> existingSchedules = findWeeklyScheduleMap(barberSchedule.getId());
        updateSchedules(existingSchedules, request);
        return weeklyScheduleMapper.toResponse(new java.util.ArrayList<>(existingSchedules.values()));
    }

    private BarberScheduleEntity findBarberSchedule(UUID barberId) {
        return barberScheduleRepository.findByBarberId(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Configuração de agenda não encontrada."));
    }

    private Map<DayOfWeek, WeeklyScheduleEntity> findWeeklyScheduleMap(UUID barberScheduleId) {
        List<WeeklyScheduleEntity> schedules = weeklyScheduleRepository.findByBarberScheduleIdOrderByDayOfWeek(barberScheduleId);
        
        Map<DayOfWeek, WeeklyScheduleEntity> scheduleMap = new java.util.HashMap<>();
        
        for (WeeklyScheduleEntity entity : schedules) {
            scheduleMap.put(entity.getDayOfWeek(), entity);
        }
        
        return scheduleMap;
    }

    private void validateRequest(WeeklyScheduleRequest request) {
        if (request.getWeeklySchedule() == null) {
            throw new IllegalArgumentException("A agenda semanal é obrigatória.");
        }

        List<WeeklyScheduleDayDto> weeklySchedule = request.getWeeklySchedule();
        
        if (weeklySchedule.size() != 7) {
            throw new IllegalArgumentException("A agenda semanal deve conter exatamente 7 dias.");
        }

        java.util.Set<DayOfWeek> daysOfWeek = new java.util.HashSet<>();
        
        for (WeeklyScheduleDayDto dto : weeklySchedule) {
            if (!daysOfWeek.add(dto.getDayOfWeek())) {
                throw new IllegalArgumentException("Existem dias da semana duplicados.");
            }
            
            if (!dto.isWorkingDay()) {
                if (dto.getStartTime() != null || dto.getEndTime() != null) {
                    throw new IllegalArgumentException("Dias não trabalhados não devem possuir horários.");
                }
            } else {
                if (dto.getStartTime() == null || dto.getEndTime() == null) {
                    throw new IllegalArgumentException("Dias trabalhados devem possuir horário inicial e final.");
                }
                
                if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                    throw new IllegalArgumentException("O horário inicial deve ser anterior ao horário final.");
                }
            }
        }
    }

    private void updateSchedules(Map<DayOfWeek, WeeklyScheduleEntity> existingSchedules, WeeklyScheduleRequest request) {
        for (WeeklyScheduleDayDto dto : request.getWeeklySchedule()) {
            WeeklyScheduleEntity entity = existingSchedules.get(dto.getDayOfWeek());
            if (entity == null) {
                throw new IllegalStateException("Agenda semanal inconsistente para o dia: " + dto.getDayOfWeek());
            }
            weeklyScheduleMapper.updateEntity(entity, dto);
        }
        
        weeklyScheduleRepository.saveAll(existingSchedules.values());
    }
}