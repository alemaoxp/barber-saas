package com.barbersaas.auth;

import com.barbersaas.auth.entity.AdminSessionEntity;
import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.auth.repository.AdminSessionRepository;
import com.barbersaas.auth.repository.AdminUserRepository;
import com.barbersaas.barbershops.entity.BarbershopEntity;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {
    private final AdminUserRepository users = mock(AdminUserRepository.class);
    private final AdminSessionRepository sessions = mock(AdminSessionRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AdminAuthProperties properties = new AdminAuthProperties();
    private final AdminAuthService auth = new AdminAuthService(users, sessions, encoder, properties);

    @Test
    void loginShouldCreateSessionWithOnlyTokenHashPersisted() {
        AdminUserEntity user = user(true);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(user));
        when(sessions.save(any(AdminSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = auth.login("admin@example.com", "secret");

        assertNotNull(response.token());
        assertEquals("Admin", response.user().name());
        verify(sessions).save(argThat(session ->
                !session.getTokenHash().equals(response.token())
                        && session.getTokenHash().length() == 64
                        && session.getExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    void loginShouldRejectUnknownEmailWrongPasswordAndInactiveUser() {
        AdminUserEntity active = user(true);
        AdminUserEntity inactive = user(false);
        when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(active));
        when(users.findByEmailIgnoreCase("inactive@example.com")).thenReturn(Optional.of(inactive));

        assertEquals("Credenciais inválidas.", assertThrows(AdminAuthException.class,
                () -> auth.login("missing@example.com", "secret")).getMessage());
        assertEquals("Credenciais inválidas.", assertThrows(AdminAuthException.class,
                () -> auth.login("admin@example.com", "wrong")).getMessage());
        assertEquals("Credenciais inválidas.", assertThrows(AdminAuthException.class,
                () -> auth.login("inactive@example.com", "secret")).getMessage());
    }

    @Test
    void authenticateShouldRequireValidNonRevokedNonExpiredSessionAndActiveUser() {
        AdminUserEntity user = user(true);
        AdminSessionEntity valid = new AdminSessionEntity(user, auth.hashToken("token"), LocalDateTime.now().plusDays(1));
        when(sessions.findByTokenHash(auth.hashToken("token"))).thenReturn(Optional.of(valid));

        assertTrue(auth.authenticate("token").isPresent());

        valid.setRevokedAt(LocalDateTime.now());
        assertTrue(auth.authenticate("token").isEmpty());

        AdminSessionEntity expired = new AdminSessionEntity(user, auth.hashToken("expired"), LocalDateTime.now().minusSeconds(1));
        when(sessions.findByTokenHash(auth.hashToken("expired"))).thenReturn(Optional.of(expired));
        assertTrue(auth.authenticate("expired").isEmpty());

        AdminUserEntity inactive = user(false);
        AdminSessionEntity inactiveSession = new AdminSessionEntity(inactive, auth.hashToken("inactive"), LocalDateTime.now().plusDays(1));
        when(sessions.findByTokenHash(auth.hashToken("inactive"))).thenReturn(Optional.of(inactiveSession));
        assertTrue(auth.authenticate("inactive").isEmpty());

        assertTrue(auth.authenticate("invalid").isEmpty());
    }

    @Test
    void logoutShouldRevokeCurrentSession() {
        AdminSessionEntity session = new AdminSessionEntity(user(true), "hash", LocalDateTime.now().plusDays(1));

        auth.logout(session);

        assertNotNull(session.getRevokedAt());
        verify(sessions).save(session);
    }

    private AdminUserEntity user(boolean active) {
        var shop = new BarbershopEntity(UUID.randomUUID(), "Jhow Cortes", true);
        var user = new AdminUserEntity(shop, "Admin", "admin@example.com", encoder.encode("secret"), active);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
