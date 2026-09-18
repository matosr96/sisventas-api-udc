package com.api.sisventas.security;

import com.api.sisventas.models.RoleName;

/**
 * Única lista de rutas abiertas de la API. Si algo no está aquí, exige token:
 * toda ruta nueva nace protegida.
 */
public final class SecurityConstants {

    public static final String HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/actuator/health",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    public static final String ROLE_ADMIN = RoleName.ADMIN.name();
    public static final String ROLE_USER = RoleName.USER.name();

    private SecurityConstants() {
    }
}
