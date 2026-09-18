package com.api.sisventas.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Única fuente de la identidad del solicitante. Las rutas la usan cuando la operación
 * depende de quién la hace; jamás se confía en un id de usuario enviado en el body.
 */
public final class Authenticated {

    public static final String ANONYMOUS = "anonymous";

    private Authenticated() {
    }

    public static String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ANONYMOUS;
        }
        return authentication.getName();
    }
}
