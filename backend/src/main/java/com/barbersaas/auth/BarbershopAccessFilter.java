package com.barbersaas.auth;

import com.barbersaas.barbers.repository.BarberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BarbershopAccessFilter extends OncePerRequestFilter {
    private static final Pattern BARBER_PATH = Pattern.compile("^/api/v1/barbers/([^/]+)(/.*)?$");
    private final BarberRepository barbers;

    public BarbershopAccessFilter(BarberRepository barbers) {
        this.barbers = barbers;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Matcher matcher = BARBER_PATH.matcher(request.getRequestURI());
        if (!matcher.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AdminPrincipal adminPrincipal)) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID barberId;
        try {
            barberId = UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException exception) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed = barbers.findById(barberId)
                .map(barber -> adminPrincipal.barbershopId().equals(barber.getBarbershop().getId()))
                .orElse(false);
        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
