package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.DailyAgendaResponse;
import com.barbersaas.appointments.dto.DailyAgendaSlotResponse;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DailyAgendaControllerTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private AppointmentService appointmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DailyAgendaController(appointmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void getDailyAgendaShouldReturnOk() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 2);
        when(appointmentService.getDailyAgenda(BARBER_ID, date))
                .thenReturn(new DailyAgendaResponse(
                        BARBER_ID,
                        date,
                        true,
                        List.of(DailyAgendaSlotResponse.free(date.atTime(9, 30)))
                ));

        mockMvc.perform(get("/api/v1/barbers/{barberId}/daily-agenda", BARBER_ID)
                        .param("date", "2026-09-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barberId", is(BARBER_ID.toString())))
                .andExpect(jsonPath("$.date", is("2026-09-02")))
                .andExpect(jsonPath("$.workingDay", is(true)))
                .andExpect(jsonPath("$.slots[0].dateTime", is("2026-09-02T09:30:00")))
                .andExpect(jsonPath("$.slots[0].status", is("FREE")));
    }
}
