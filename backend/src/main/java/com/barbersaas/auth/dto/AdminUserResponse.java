package com.barbersaas.auth.dto;

import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String name,
        String email,
        UUID barbershopId,
        String barbershopName
) {
}
