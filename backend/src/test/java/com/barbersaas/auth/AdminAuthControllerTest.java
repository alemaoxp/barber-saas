package com.barbersaas.auth;

import com.barbersaas.auth.entity.AdminSessionEntity;
import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.auth.repository.AdminSessionRepository;
import com.barbersaas.auth.repository.AdminUserRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import com.barbersaas.barbershops.repository.BarbershopRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminAuthService authService;

    @Autowired
    private AdminSessionRepository sessions;

    @Autowired
    private AdminUserRepository users;

    @Autowired
    private BarbershopRepository barbershops;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<AdminSessionEntity> createdSessions = new ArrayList<>();
    private final List<AdminUserEntity> createdUsers = new ArrayList<>();
    private final List<BarbershopEntity> createdShops = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        sessions.deleteAll(createdSessions);
        users.deleteAll(createdUsers);
        barbershops.deleteAll(createdShops);
    }

    @Test
    void meShouldReturnCurrentAdminWithLazyBarbershopLoaded() throws Exception {
        String token = "valid-token-" + UUID.randomUUID();
        AdminUserEntity user = adminUser();
        AdminSessionEntity session = sessions.save(new AdminSessionEntity(
                user,
                authService.hashToken(token),
                LocalDateTime.now().plusDays(1)
        ));
        createdSessions.add(session);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.barbershopId").value(user.getBarbershop().getId().toString()))
                .andExpect(jsonPath("$.barbershopName").value(user.getBarbershop().getName()));
    }

    @Test
    void meShouldReturnUnauthorizedForInvalidSession() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private AdminUserEntity adminUser() {
        BarbershopEntity shop = barbershops.save(new BarbershopEntity(
                UUID.randomUUID(),
                "Shop Teste",
                true
        ));
        AdminUserEntity user = users.save(new AdminUserEntity(
                shop,
                "Admin Teste",
                "admin-" + UUID.randomUUID() + "@local.test",
                passwordEncoder.encode("secret"),
                true
        ));
        createdShops.add(shop);
        createdUsers.add(user);
        return user;
    }
}
