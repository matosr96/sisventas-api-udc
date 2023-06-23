package com.api.sisventas.dtos;

import lombok.Data;

@Data
public class DtoRegistro {
    private String nombre;
    private String apellido;
    private String foto;
    private String username;
    private String password;
}