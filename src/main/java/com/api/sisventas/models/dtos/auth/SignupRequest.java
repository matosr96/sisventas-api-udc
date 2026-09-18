package com.api.sisventas.models.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min = 3, max = 50) String firstName,
        @NotBlank @Size(min = 3, max = 50) String lastName,
        String photo,
        @NotBlank @Size(min = 5, max = 100) String username,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
