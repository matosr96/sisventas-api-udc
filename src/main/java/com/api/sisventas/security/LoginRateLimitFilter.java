package com.api.sisventas.security;

import com.api.sisventas.common.ErrorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Frena la fuerza bruta contra el inicio de sesión: un máximo de intentos por IP dentro de una
 * ventana deslizante. BCrypt hace lento cada intento, pero sin un tope nada impide probar sin fin.
 *
 * El estado vive en memoria: es suficiente para una sola instancia. Detrás de un proxy hay que
 * hacer que la IP real llegue en {@code request.getRemoteAddr()} (por ejemplo con
 * {@code server.forward-headers-strategy=framework}), o el límite contaría a todos como uno.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String SIGNIN_PATH = "/api/v1/auth/signin";
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> attemptsByClient = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isSignin(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (tooManyAttempts(request.getRemoteAddr())) {
            reject(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isSignin(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) && SIGNIN_PATH.equals(request.getRequestURI());
    }

    private boolean tooManyAttempts(String client) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(WINDOW);
        Deque<Instant> attempts = attemptsByClient.computeIfAbsent(client, key -> new ConcurrentLinkedDeque<>());
        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
                attempts.pollFirst();
            }
            if (attempts.size() >= MAX_ATTEMPTS) {
                return true;
            }
            attempts.addLast(now);
            return false;
        }
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("message", ErrorCodes.TOO_MANY_REQUESTS));
    }
}
