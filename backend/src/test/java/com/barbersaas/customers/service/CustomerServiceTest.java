package com.barbersaas.customers.service;

import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.customers.dto.CustomerSummaryResponse;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.mapper.CustomerMapper;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final UUID BARBER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SECOND_CUSTOMER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BarberRepository barberRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
                customerRepository,
                new CustomerMapper(),
                barberRepository
        );
    }

    @Test
    void shouldListCustomersAssociatedWithBarber() {
        CustomerEntity customer = customer(
                CUSTOMER_ID,
                "Gabriel Santana",
                "(13) 99999-9999"
        );

        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberId(BARBER_ID))
                .thenReturn(List.of(customer));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, null);

        assertEquals(1, response.size());
        assertEquals(CUSTOMER_ID, response.get(0).getId());
        assertEquals("Gabriel Santana", response.get(0).getName());
        assertEquals("(13) 99999-9999", response.get(0).getPhone());
    }

    @Test
    void shouldKeepRepositoryDistinctAndBarberScope() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberId(BARBER_ID))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Ana Lima", "(13) 90000-0001")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, null);

        assertEquals(1, response.size());
        assertEquals(CUSTOMER_ID, response.get(0).getId());
        verify(customerRepository).findDistinctByBarberId(BARBER_ID);
        verify(customerRepository, never())
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%Ana%");
    }

    @Test
    void shouldTreatEmptyQueryAsNoFilter() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberId(BARBER_ID))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Ana Lima", "(13) 90000-0001")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, "");

        assertEquals(1, response.size());
        verify(customerRepository).findDistinctByBarberId(BARBER_ID);
        verify(customerRepository, never())
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%%");
    }

    @Test
    void shouldTreatBlankQueryAsNoFilter() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberId(BARBER_ID))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Ana Lima", "(13) 90000-0001")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, "   ");

        assertEquals(1, response.size());
        verify(customerRepository).findDistinctByBarberId(BARBER_ID);
        verify(customerRepository, never())
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%   %");
    }

    @Test
    void shouldPassNameQueryTrimmedAsLikePattern() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberIdAndQuery(BARBER_ID, "%gabriel%"))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Gabriel Santana", "(13) 99999-9999")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, "  gabriel  ");

        assertEquals("Gabriel Santana", response.get(0).getName());
        verify(customerRepository)
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%gabriel%");
        verify(customerRepository, never()).findDistinctByBarberId(BARBER_ID);
    }

    @Test
    void shouldPassCaseInsensitiveNameQueryToRepository() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberIdAndQuery(BARBER_ID, "%GABRIEL%"))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Gabriel Santana", "(13) 99999-9999")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, "GABRIEL");

        assertEquals("Gabriel Santana", response.get(0).getName());
        verify(customerRepository)
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%GABRIEL%");
    }

    @Test
    void shouldPassPhoneQueryToRepository() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberIdAndQuery(BARBER_ID, "%9999%"))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Gabriel Santana", "(13) 99999-9999")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, "9999");

        assertEquals("(13) 99999-9999", response.get(0).getPhone());
        verify(customerRepository)
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%9999%");
    }

    @Test
    void shouldSortCustomersAlphabeticallyByName() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberId(BARBER_ID))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "Bruno Costa", "(13) 90000-0002"),
                        customer(SECOND_CUSTOMER_ID, "Ana Lima", "(13) 90000-0001")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, null);

        assertEquals("Ana Lima", response.get(0).getName());
        assertEquals("Bruno Costa", response.get(1).getName());
    }

    @Test
    void shouldSortCustomersAlphabeticallyByNameIgnoringCase() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(true);
        when(customerRepository.findDistinctByBarberId(BARBER_ID))
                .thenReturn(List.of(
                        customer(CUSTOMER_ID, "bruno Costa", "(13) 90000-0002"),
                        customer(SECOND_CUSTOMER_ID, "ana Lima", "(13) 90000-0001")
                ));

        List<CustomerSummaryResponse> response =
                customerService.findSummariesByBarber(BARBER_ID, null);

        assertEquals("ana Lima", response.get(0).getName());
        assertEquals("bruno Costa", response.get(1).getName());
    }

    @Test
    void shouldFailWhenBarberDoesNotExist() {
        when(barberRepository.existsById(BARBER_ID)).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> customerService.findSummariesByBarber(BARBER_ID, null)
        );

        assertEquals("Barbeiro não encontrado.", exception.getMessage());
        verify(customerRepository, never())
                .findDistinctByBarberId(BARBER_ID);
        verify(customerRepository, never())
                .findDistinctByBarberIdAndQuery(BARBER_ID, "%gabriel%");
    }

    private CustomerEntity customer(UUID id, String name, String phone) {
        CustomerEntity customer =
                new CustomerEntity(name, phone, null, null, null, true);
        ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }
}
