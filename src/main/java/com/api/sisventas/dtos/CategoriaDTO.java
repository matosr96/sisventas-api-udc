package com.api.sisventas.dtos;

import lombok.Data;

@Data
public class CategoriaDTO {
    private Long id;
    private String nombre;
    private String icono;
    private Long userId;
}
