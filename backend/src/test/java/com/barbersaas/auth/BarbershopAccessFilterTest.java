package com.barbersaas.auth;

import com.barbersaas.auth.entity.AdminSessionEntity;
import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BarbershopAccessFilterTest {
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowAdminBarberAndForbidOtherBarbershop() throws Exception {
        UUID shopId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        BarberRepository barbers = mock(BarberRepository.class);
        when(barbers.findById(barberId)).thenReturn(Optional.of(barber(barberId, UUID.randomUUID())));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal(shopId),
                null
        ));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/barbers/" + barberId + "/services");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        new BarbershopAccessFilter(barbers).doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);

        when(barbers.findById(barberId)).thenReturn(Optional.of(barber(barberId, shopId)));
        response = new MockHttpServletResponse();
        new BarbershopAccessFilter(barbers).doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(eq(request), any());
    }

    private AdminPrincipal principal(UUID shopId) {
        var shop = new BarbershopEntity(shopId, "Jhow Cortes", true);
        var user = new AdminUserEntity(shop, "Admin", "admin@example.com", "hash", true);
        var session = new AdminSessionEntity(user, "hash", LocalDateTime.now().plusDays(1));
        return new AdminPrincipal(user, session);
    }

    private BarberEntity barber(UUID barberId, UUID shopId) {
        var barber = new BarberEntity("Jhow", "jhow@example.com", "(11) 99999-9999", null, true);
        barber.setId(barberId);
        barber.setBarbershop(new BarbershopEntity(shopId, "Shop", true));
        return barber;
    }
}
