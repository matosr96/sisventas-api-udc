package com.api.sisventas.models.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record SigninRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
