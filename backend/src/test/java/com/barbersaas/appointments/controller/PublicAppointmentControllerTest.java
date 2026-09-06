package com.barbersaas.appointments.controller;

import com.barbersaas.appointments.dto.PublicAppointmentResponse;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.appointments.service.PublicAppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicAppointmentControllerTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SERVICE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPOINTMENT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID CANCEL_TOKEN =
            UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Mock
    private PublicAppointmentService publicAppointmentService;

    @Mock
    private AppointmentService appointmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new PublicAppointmentController(
                                publicAppointmentService,
                                appointmentService
                        )
                )
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void publicCreateShouldReturnCancelToken() throws Exception {
        LocalDateTime dateTime =
                LocalDateTime.of(2026, 9, 9, 10, 0);
        when(publicAppointmentService.createPublicAppointment(
                eq(BARBER_ID),
                any()
        )).thenReturn(
                new PublicAppointmentResponse(
                        APPOINTMENT_ID,
                        CUSTOMER_ID,
                        List.of(SERVICE_ID),
                        BigDecimal.valueOf(40),
                        dateTime,
                        AppointmentStatus.SCHEDULED,
                        "teste",
                        LocalDateTime.of(2026, 9, 5, 10, 0),
                        CANCEL_TOKEN.toString()
                )
        );

        mockMvc.perform(post("/api/public/appointments")
                        .param("barberId", BARBER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Cliente",
                                  "customerPhone": "(11) 97777-1234",
                                  "serviceIds": ["%s"],
                                  "appointmentDateTime": "%s",
                                  "notes": "teste"
                                }
                                """.formatted(SERVICE_ID, dateTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(APPOINTMENT_ID.toString())))
                .andExpect(jsonPath("$.cancelToken", is(CANCEL_TOKEN.toString())));
    }

    @Test
    void publicCancelShouldUseCancelToken() throws Exception {
        doNothing().when(appointmentService)
                .cancelByToken(CANCEL_TOKEN);

        mockMvc.perform(delete(
                        "/api/public/appointments/{token}",
                        CANCEL_TOKEN
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicAppointmentsShouldExposeCustomerScopedLookup() throws Exception {
        mockMvc.perform(get("/api/public/appointments")
                        .param("phone", "(11) 97777-1234"))
                .andExpect(status().isOk());
    }
}
