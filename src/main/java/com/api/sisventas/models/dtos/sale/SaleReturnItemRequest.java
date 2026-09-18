package com.api.sisventas.models.dtos.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Qué línea de la venta se devuelve y cuántas unidades. */
public record SaleReturnItemRequest(
        @NotNull Long saleItemId,
        @NotNull @Positive Integer quantity) {
}
