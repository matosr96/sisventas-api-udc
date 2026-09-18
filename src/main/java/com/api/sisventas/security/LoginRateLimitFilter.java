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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frena la fuerza bruta y el registro masivo: un tope de intentos por IP y ruta dentro
 * de una ventana deslizante. BCrypt hace lento cada intento de login, pero sin un tope
 * nada impide probar sin fin; y sin tope en el registro cualquiera infla la tabla de usuarios.
 *
 * El estado vive en memoria y las colas vacías se eliminan al pasar por ellas, así que el
 * mapa no crece con cada IP que alguna vez llamó. Suficiente para una instancia. Detrás de
 * un proxy hay que hacer que la IP real llegue en {@code request.getRemoteAddr()} (por
 * ejemplo con {@code server.forward-headers-strategy=framework}), o el límite contaría a
 * todos como uno.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    /** Ruta protegida y cuántos intentos admite por IP dentro de la ventana. */
    private record Limit(String path, int maxAttempts) {
    }

    static final Duration WINDOW = Duration.ofMinutes(1);
    static final int SIGNIN_MAX_ATTEMPTS = 10;
    static final int SIGNUP_MAX_ATTEMPTS = 5;

    private static final Map<String, Limit> LIMITS = Map.of(
            "/api/v1/auth/signin", new Limit("/api/v1/auth/signin", SIGNIN_MAX_ATTEMPTS),
            "/api/v1/auth/signup", new Limit("/api/v1/auth/signup", SIGNUP_MAX_ATTEMPTS));

    private final Map<String, Deque<Instant>> attemptsByClientAndPath = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Limit limit = "POST".equals(request.getMethod()) ? LIMITS.get(request.getRequestURI()) : null;
        if (limit != null && tooManyAttempts(request.getRemoteAddr(), limit)) {
            reject(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean tooManyAttempts(String client, Limit limit) {
        String key = client + " " + limit.path();
        Instant now = Instant.now();
        Instant windowStart = now.minus(WINDOW);
        // compute() serializa el acceso a la cola de esta clave y devuelve null para eliminarla.
        boolean[] rejected = {false};
        attemptsByClientAndPath.compute(key, (ignored, attempts) -> {
            Deque<Instant> queue = attempts == null ? new ArrayDeque<>() : attempts;
            while (!queue.isEmpty() && queue.peekFirst().isBefore(windowStart)) {
                queue.pollFirst();
            }
            if (queue.size() >= limit.maxAttempts()) {
                rejected[0] = true;
            } else {
                queue.addLast(now);
            }
            return queue.isEmpty() ? null : queue;
        });
        return rejected[0];
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("message", ErrorCodes.TOO_MANY_REQUESTS));
    }
}
