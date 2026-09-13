package com.barbersaas.auth;

import com.barbersaas.auth.dto.AdminLoginRequest;
import com.barbersaas.auth.dto.AdminLoginResponse;
import com.barbersaas.auth.dto.AdminUserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {
    private final AdminAuthService authService;

    public AdminAuthController(AdminAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @GetMapping("/me")
    public AdminUserResponse me(Authentication authentication) {
        AdminPrincipal principal = (AdminPrincipal) authentication.getPrincipal();
        return authService.toUserResponse(principal.user());
    }

    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        AdminPrincipal principal = (AdminPrincipal) authentication.getPrincipal();
        authService.logout(principal.session());
    }
}
