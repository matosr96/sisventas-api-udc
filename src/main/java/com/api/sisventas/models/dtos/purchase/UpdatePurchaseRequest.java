package com.api.sisventas.models.dtos.purchase;

import java.time.Instant;

/** Lo único corregible de una compra registrada es su fecha; para rectificarla se anula y se registra de nuevo. */
public record UpdatePurchaseRequest(Instant purchaseDate) {
}
