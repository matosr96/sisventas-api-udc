package com.api.sisventas.models.dtos.purchase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Línea de compra. A diferencia de la venta, el costo sí lo pone el cliente: es lo que cobró el proveedor. */
public record PurchaseItemRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity,
        @NotNull @PositiveOrZero BigDecimal unitCost) {
}
