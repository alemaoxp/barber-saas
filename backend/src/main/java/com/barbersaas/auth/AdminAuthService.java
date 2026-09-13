package com.barbersaas.auth;

import com.barbersaas.auth.dto.AdminLoginResponse;
import com.barbersaas.auth.dto.AdminUserResponse;
import com.barbersaas.auth.entity.AdminSessionEntity;
import com.barbersaas.auth.entity.AdminUserEntity;
import com.barbersaas.auth.repository.AdminSessionRepository;
import com.barbersaas.auth.repository.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AdminAuthService {
    private static final String INVALID_CREDENTIALS = "Credenciais inválidas.";

    private final AdminUserRepository users;
    private final AdminSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminAuthService(
            AdminUserRepository users,
            AdminSessionRepository sessions,
            PasswordEncoder passwordEncoder,
            AdminAuthProperties properties) {
        this.users = users;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Transactional
    public AdminLoginResponse login(String email, String password) {
        AdminUserEntity user = users.findByEmailIgnoreCase(email.trim())
                .filter(admin -> Boolean.TRUE.equals(admin.getActive()))
                .filter(admin -> passwordEncoder.matches(password, admin.getPasswordHash()))
                .orElseThrow(() -> new AdminAuthException(INVALID_CREDENTIALS));

        String token = generateToken();
        AdminSessionEntity session = sessions.save(new AdminSessionEntity(
                user,
                hashToken(token),
                LocalDateTime.now().plusDays(properties.getSessionDurationDays())
        ));
        return new AdminLoginResponse(token, toUserResponse(session.getAdminUser()));
    }

    @Transactional(readOnly = true)
    public Optional<AdminPrincipal> authenticate(String token) {
        return sessions.findByTokenHash(hashToken(token))
                .filter(this::isValid)
                .map(session -> new AdminPrincipal(session.getAdminUser(), session));
    }

    @Transactional
    public void logout(AdminSessionEntity session) {
        session.setRevokedAt(LocalDateTime.now());
        sessions.save(session);
    }

    public AdminUserResponse toUserResponse(AdminUserEntity user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBarbershop().getId(),
                user.getBarbershop().getName()
        );
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível validar a sessão.");
        }
    }

    private boolean isValid(AdminSessionEntity session) {
        return session.getRevokedAt() == null
                && session.getExpiresAt().isAfter(LocalDateTime.now())
                && Boolean.TRUE.equals(session.getAdminUser().getActive());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
