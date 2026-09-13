package com.barbersaas.auth;

import com.barbersaas.auth.entity.AdminSessionEntity;
import com.barbersaas.auth.entity.AdminUserEntity;

import java.util.UUID;

public class AdminPrincipal {
    private final AdminUserEntity user;
    private final AdminSessionEntity session;

    public AdminPrincipal(AdminUserEntity user, AdminSessionEntity session) {
        this.user = user;
        this.session = session;
    }

    public AdminUserEntity user() { return user; }
    public AdminSessionEntity session() { return session; }
    public UUID barbershopId() { return user.getBarbershop().getId(); }
}
