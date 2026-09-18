package com.api.sisventas.models.dtos.purchase;

import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

/** Lo único corregible de una compra registrada es su fecha; para rectificarla se anula y se registra de nuevo. */
public record UpdatePurchaseRequest(@PastOrPresent Instant purchaseDate) {
}
