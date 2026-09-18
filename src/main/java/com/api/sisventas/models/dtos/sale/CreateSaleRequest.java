package com.api.sisventas.models.dtos.sale;

import com.api.sisventas.models.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * El subtotal, el impuesto y el total no se reciben: se calculan desde las líneas y la
 * tasa configurada. El cliente solo dice qué vendió, cuánto descontó y cómo cobró.
 * {@code paymentMethod} nulo es efectivo; {@code amountPaid} solo se usa en efectivo.
 */
public record CreateSaleRequest(
        @PastOrPresent Instant saleDate,
        @NotEmpty @Valid List<SaleItemRequest> items,
        @PositiveOrZero BigDecimal discount,
        PaymentMethod paymentMethod,
        @PositiveOrZero BigDecimal amountPaid,
        @Size(max = 120) String customerName) {
}
