package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.AppointmentResponse;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SERVICE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPOINTMENT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Mock
    private AppointmentService appointmentService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AppointmentController(appointmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void validPostShouldReturnCreated() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 9, 30);
        when(appointmentService.create(eq(BARBER_ID), any()))
                .thenReturn(response(dateTime));

        mockMvc.perform(post("/api/v1/barbers/{barberId}/appointments", BARBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "serviceId": "%s",
                                  "appointmentDateTime": "%s",
                                  "notes": "teste"
                                }
                                """.formatted(CUSTOMER_ID, SERVICE_ID, dateTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId", is(CUSTOMER_ID.toString())))
                .andExpect(jsonPath("$.serviceId", is(SERVICE_ID.toString())))
                .andExpect(jsonPath("$.status", is("SCHEDULED")));
    }

    @Test
    void invalidBusinessRuleShouldReturnBadRequest() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 31, 20, 1);
        when(appointmentService.create(eq(BARBER_ID), any()))
                .thenThrow(new BusinessException("Horário fora do expediente."));

        mockMvc.perform(post("/api/v1/barbers/{barberId}/appointments", BARBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "serviceId": "%s",
                                  "appointmentDateTime": "%s",
                                  "notes": "teste"
                                }
                                """.formatted(CUSTOMER_ID, SERVICE_ID, dateTime)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Horário fora do expediente.")));
    }

    @Test
    void validGetAppointmentsShouldReturnOk() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 9, 30);
        when(appointmentService.findAll(BARBER_ID))
                .thenReturn(List.of(response(dateTime)));

        mockMvc.perform(get("/api/v1/barbers/{barberId}/appointments", BARBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId", is(CUSTOMER_ID.toString())))
                .andExpect(jsonPath("$[0].serviceId", is(SERVICE_ID.toString())));
    }

    @Test
    void validPutShouldReturnOk() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 10, 0);
        when(appointmentService.update(eq(BARBER_ID), eq(APPOINTMENT_ID), any()))
                .thenReturn(response(dateTime));

        mockMvc.perform(put(
                        "/api/v1/barbers/{barberId}/appointments/{appointmentId}",
                        BARBER_ID,
                        APPOINTMENT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "serviceId": "%s",
                                  "appointmentDateTime": "%s",
                                  "notes": "alterado"
                                }
                                """.formatted(CUSTOMER_ID, SERVICE_ID, dateTime)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentDateTime", is("2026-09-02T10:00:00")));
    }

    @Test
    void validDeleteShouldReturnNoContent() throws Exception {
        doNothing().when(appointmentService)
                .delete(BARBER_ID, APPOINTMENT_ID);

        mockMvc.perform(delete(
                        "/api/v1/barbers/{barberId}/appointments/{appointmentId}",
                        BARBER_ID,
                        APPOINTMENT_ID
                ))
                .andExpect(status().isNoContent());
    }

    private AppointmentResponse response(LocalDateTime dateTime) {
        return new AppointmentResponse(
                APPOINTMENT_ID,
                CUSTOMER_ID,
                SERVICE_ID,
                dateTime,
                AppointmentStatus.SCHEDULED,
                "teste",
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }
}
