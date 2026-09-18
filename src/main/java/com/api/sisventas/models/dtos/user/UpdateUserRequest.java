package com.api.sisventas.models.dtos.user;

import jakarta.validation.constraints.Size;

/** Datos personales. Parcial: nulo = no tocar. El usuario y la contraseña van por rutas propias. */
public record UpdateUserRequest(
        @Size(min = 3, max = 50) String firstName,
        @Size(min = 3, max = 50) String lastName,
        @Size(max = 512) String photo) {
}
