package com.api.sisventas.models.dtos.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Línea pedida por el cliente. El precio no viaja aquí: lo pone el servidor. */
public record SaleItemRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity) {
}
