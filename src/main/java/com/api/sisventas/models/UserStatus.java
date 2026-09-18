package com.api.sisventas.models;

/** Estado de una cuenta. Un usuario INACTIVE no puede iniciar sesión ni usar tokens vigentes. */
public enum UserStatus {
    ACTIVE,
    INACTIVE
}
