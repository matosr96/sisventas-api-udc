package com.api.sisventas.models.dtos.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

/**
 * El total no se recibe: se calcula desde las líneas. Antes lo enviaba el cliente y el
 * servidor lo aceptaba sin comprobar nada.
 */
public record CreateSaleRequest(
        Instant saleDate,
        @NotEmpty @Valid List<SaleItemRequest> items) {
}
