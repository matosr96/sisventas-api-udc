package com.api.sisventas.models.dtos.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corrección manual del stock: positivo suma, negativo resta. El motivo es obligatorio
 * porque un ajuste sin explicación es exactamente lo que el libro existe para evitar.
 */
public record AdjustStockRequest(
        @NotNull Integer quantity,
        @NotBlank @Size(max = 255) String reason) {
}
