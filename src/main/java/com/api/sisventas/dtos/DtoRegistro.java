package com.api.sisventas.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DtoRegistro {
    @NotNull
    @Size(min = 3, max = 50)
    private String nombre;

    @NotNull
    @Size(min = 3, max = 50)
    private String apellido;

    private String foto;

    @NotNull
    @Size(min = 5, max = 100)
    private String username;

    @NotNull
    @Size(min = 8, max = 100)
    private String password;
}