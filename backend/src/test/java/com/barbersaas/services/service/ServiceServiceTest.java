package com.barbersaas.services.service;

import com.barbersaas.services.entity.ServiceEntity;
import com.barbersaas.services.mapper.ServiceMapper;
import com.barbersaas.services.repository.ServiceRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceServiceTest {

    @Test
    void findActiveShouldExcludeInactiveServices() {
        ServiceRepository repository = mock(ServiceRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new ServiceEntity("Corte", "", 30, BigDecimal.valueOf(40), true),
                new ServiceEntity("Inativo", "", 30, BigDecimal.valueOf(30), false)
        ));

        var responses = new ServiceService(repository, new ServiceMapper()).findActive();

        assertEquals(List.of("Corte"), responses.stream().map(response -> response.getName()).toList());
    }
}
