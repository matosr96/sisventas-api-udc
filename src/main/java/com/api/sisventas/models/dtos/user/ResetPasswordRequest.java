package com.api.sisventas.models.dtos.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Reinicio por un administrador: no exige la contraseña actual, pero cierra las sesiones abiertas. */
public record ResetPasswordRequest(@NotBlank @Size(min = 8, max = 100) String newPassword) {
}
