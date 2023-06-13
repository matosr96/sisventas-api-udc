package com.api.sisventas.dtos;

import com.api.sisventas.entidades.Usuarios;

public class AuthResponseDto {
    private String token;
    private Usuarios usuarios;

    public AuthResponseDto(String token, Usuarios usuarios) {
        this.token = token;
        this.usuarios = usuarios;
    }
}
