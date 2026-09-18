package com.api.sisventas.models.dtos.user;

import com.api.sisventas.models.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta hecha por un administrador: elige el rol de entrada; nulo = vendedor. */
public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String firstName,
        @NotBlank @Size(min = 3, max = 50) String lastName,
        @Size(max = 512) String photo,
        @NotBlank @Size(min = 5, max = 100) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        RoleName role) {
}
