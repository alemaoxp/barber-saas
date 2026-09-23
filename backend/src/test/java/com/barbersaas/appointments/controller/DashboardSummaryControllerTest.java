package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.DashboardSummaryResponse;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardSummaryControllerTest {
    private static final UUID BARBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private AppointmentService appointmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DashboardSummaryController(appointmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getSummaryShouldReturnScheduledValueForInclusivePeriod() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 30);
        when(appointmentService.getDashboardSummary(BARBER_ID, startDate, endDate))
                .thenReturn(new DashboardSummaryResponse(BARBER_ID, startDate, endDate, 3, BigDecimal.valueOf(150)));

        mockMvc.perform(get("/api/v1/barbers/{barberId}/dashboard/summary", BARBER_ID)
                        .param("startDate", "2026-09-01")
                        .param("endDate", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barberId", is(BARBER_ID.toString())))
                .andExpect(jsonPath("$.startDate", is("2026-09-01")))
                .andExpect(jsonPath("$.endDate", is("2026-09-30")))
                .andExpect(jsonPath("$.appointmentCount", is(3)))
                .andExpect(jsonPath("$.scheduledValue", is(150)));
    }
}
