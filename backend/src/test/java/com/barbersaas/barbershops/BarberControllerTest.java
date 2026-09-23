package com.barbersaas.barbershops;

import com.barbersaas.auth.AdminPrincipal;
import com.barbersaas.auth.entity.AdminSessionEntity;
import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.barbers.controller.BarberController;
import com.barbersaas.barbers.dto.BarberResponse;
import com.barbersaas.barbers.dto.CreateBarberRequest;
import com.barbersaas.barbers.service.BarberService;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarberControllerTest {

    private static final UUID SHOP_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private BarberService barberService;

    @Test
    void listUsesAuthenticatedAdminBarbershop() {
        BarberResponse response = new BarberResponse();
        when(barberService.findAll(SHOP_A_ID)).thenReturn(List.of(response));

        var result = new BarberController(barberService).findAll(principal());

        assertEquals(List.of(response), result.getBody());
        verify(barberService).findAll(SHOP_A_ID);
    }

    @Test
    void createUsesAuthenticatedAdminBarbershop() {
        CreateBarberRequest request = new CreateBarberRequest();
        BarberResponse response = new BarberResponse();
        when(barberService.create(SHOP_A_ID, request)).thenReturn(response);

        var result = new BarberController(barberService).create(request, principal());

        assertEquals(response, result.getBody());
        verify(barberService).create(SHOP_A_ID, request);
    }

    private AdminPrincipal principal() {
        BarbershopEntity shop = new BarbershopEntity(SHOP_A_ID, "Shop A", true);
        AdminUserEntity user = new AdminUserEntity(shop, "Admin A", "admin-a@example.com", "hash", true);
        AdminSessionEntity session = new AdminSessionEntity(user, "token", LocalDateTime.now().plusHours(1));
        return new AdminPrincipal(user, session);
    }
}
