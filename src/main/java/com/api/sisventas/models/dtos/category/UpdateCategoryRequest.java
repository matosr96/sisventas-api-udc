package com.api.sisventas.models.dtos.category;

import jakarta.validation.constraints.Size;

/** Actualización parcial: solo se aplican los campos presentes (no nulos). */
public record UpdateCategoryRequest(
        @Size(max = 120) String name,
        String icon) {
}
