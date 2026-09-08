package com.barbersaas.push.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record PushSubscriptionRequest(
        @NotBlank String endpoint,
        @NotBlank String p256dh,
        @NotBlank String auth,
        UUID customerId) {
}
