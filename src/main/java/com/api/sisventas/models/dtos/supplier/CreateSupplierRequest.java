package com.api.sisventas.models.dtos.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 50) String taxId,
        @Size(max = 30) String phone,
        @Email @Size(max = 120) String email) {
}
