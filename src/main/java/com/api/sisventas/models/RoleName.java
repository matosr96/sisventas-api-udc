package com.api.sisventas.models;

/**
 * Roles del sistema. Los nombres viajan tal cual como authorities de Spring Security,
 * así que la matriz de {@code SecurityConfig} depende de ellos: por eso son un enum
 * con un CHECK en la tabla y no texto libre.
 */
public enum RoleName {
    USER,
    ADMIN
}
