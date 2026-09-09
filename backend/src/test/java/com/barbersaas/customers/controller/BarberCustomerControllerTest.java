package com.barbersaas.customers.controller;

import com.barbersaas.customers.dto.CustomerSummaryResponse;
import com.barbersaas.customers.service.CustomerService;
import com.barbersaas.exception.GlobalExceptionHandler;
import com.barbersaas.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BarberCustomerControllerTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BarberCustomerController(customerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldReturnOnlyCustomerSummaryFields() throws Exception {
        when(customerService.findSummariesByBarber(BARBER_ID, null))
                .thenReturn(List.of(new CustomerSummaryResponse(
                        CUSTOMER_ID,
                        "Gabriel Santana",
                        "(13) 99999-9999"
                )));

        mockMvc.perform(get("/api/v1/barbers/{barberId}/customers", BARBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(CUSTOMER_ID.toString())))
                .andExpect(jsonPath("$[0].name", is("Gabriel Santana")))
                .andExpect(jsonPath("$[0].phone", is("(13) 99999-9999")))
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].birthDate").doesNotExist())
                .andExpect(jsonPath("$[0].notes").doesNotExist())
                .andExpect(jsonPath("$[0].active").doesNotExist());
    }

    @Test
    void shouldAcceptQueryParameter() throws Exception {
        when(customerService.findSummariesByBarber(BARBER_ID, "gabriel"))
                .thenReturn(List.of(new CustomerSummaryResponse(
                        CUSTOMER_ID,
                        "Gabriel Santana",
                        "(13) 99999-9999"
                )));

        mockMvc.perform(get("/api/v1/barbers/{barberId}/customers", BARBER_ID)
                        .param("query", "gabriel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Gabriel Santana")));
    }

    @Test
    void shouldReturnNotFoundWhenBarberDoesNotExist() throws Exception {
        when(customerService.findSummariesByBarber(BARBER_ID, null))
                .thenThrow(new NotFoundException("Barbeiro não encontrado."));

        mockMvc.perform(get("/api/v1/barbers/{barberId}/customers", BARBER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Barbeiro não encontrado.")));
    }
}
