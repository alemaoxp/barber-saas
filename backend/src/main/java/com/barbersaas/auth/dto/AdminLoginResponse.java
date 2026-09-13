package com.barbersaas.auth.dto;

public record AdminLoginResponse(String token, AdminUserResponse user) {
}
