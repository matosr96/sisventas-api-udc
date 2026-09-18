package com.api.sisventas.models.dtos.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** El total no se recibe: se calcula desde las líneas. */
public record CreatePurchaseRequest(
        @NotNull Long supplierId,
        Instant purchaseDate,
        @NotEmpty @Valid List<PurchaseItemRequest> items) {
}
