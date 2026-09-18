package com.api.sisventas.models.dtos.supplier;

import com.api.sisventas.models.SupplierStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Actualización parcial: solo se aplican los campos presentes (no nulos). */
public record UpdateSupplierRequest(
        @Size(max = 120) String name,
        @Size(max = 50) String taxId,
        @Size(max = 30) String phone,
        @Email @Size(max = 120) String email,
        SupplierStatus status) {
}
