package com.api.sisventas.dtos;

import com.api.sisventas.entidades.Usuarios;
import lombok.Data;

@Data
public class DtoAuthRespuesta {
    private String accessToken;
    private String tokenType = "Bearer ";
    private Usuarios user;

    public DtoAuthRespuesta(String accessToken, Usuarios user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}