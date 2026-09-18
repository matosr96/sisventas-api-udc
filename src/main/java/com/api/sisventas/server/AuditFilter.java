package com.api.sisventas.server;

import com.api.sisventas.common.Authenticated;
import com.api.sisventas.dataSources.AuditRepository;
import com.api.sisventas.models.Audit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Registra toda escritura exitosa. Es el único sitio que escribe en {@code audits}:
 * ninguna ruta audita por su cuenta y nunca se guarda el cuerpo de la petición,
 * porque ahí viajan las contraseñas.
 */
@Component
@Order(Integer.MAX_VALUE)
public class AuditFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuditFilter.class);
    private static final Set<String> WRITES = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String AUTH_PREFIX = "/api/v1/auth";
    private static final int FIRST_ERROR_STATUS = 400;

    private final AuditRepository auditRepository;

    public AuditFilter(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (isAuditable(request, response)) {
            record(request);
        }
    }

    private boolean isAuditable(HttpServletRequest request, HttpServletResponse response) {
        return WRITES.contains(request.getMethod())
                && response.getStatus() < FIRST_ERROR_STATUS
                && !request.getRequestURI().startsWith(AUTH_PREFIX);
    }

    /** Un fallo de auditoría nunca puede tumbar la petición que ya se respondió. */
    private void record(HttpServletRequest request) {
        try {
            Audit audit = new Audit();
            audit.setUsername(Authenticated.username());
            audit.setMethod(request.getMethod());
            audit.setResource(request.getRequestURI());
            audit.setDetail(request.getQueryString());
            auditRepository.save(audit);
        } catch (Exception error) {
            LOG.error("No se pudo registrar la auditoría de {} {}",
                    request.getMethod(), request.getRequestURI(), error);
        }
    }
}
