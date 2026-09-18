package com.api.sisventas.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Principal con la versión de token de la cuenta. El JWT lleva esa versión y el filtro la
 * compara con la actual: si la cuenta la subió (cerrar sesión en todos los dispositivos,
 * contraseña reiniciada), todos los tokens anteriores dejan de valer sin lista negra.
 */
public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    private final int tokenVersion;

    public AuthenticatedUser(String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, enabled, true, true, true, authorities);
        this.tokenVersion = tokenVersion;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }
}
