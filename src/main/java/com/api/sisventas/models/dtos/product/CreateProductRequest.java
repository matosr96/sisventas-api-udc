package com.api.sisventas.models.dtos.product;

import com.api.sisventas.models.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 120) String name,
        @PositiveOrZero BigDecimal purchasePrice,
        @NotNull @PositiveOrZero BigDecimal salePrice,
        /** Unidades con las que nace. Queda como asiento INITIAL del libro de stock. */
        @NotNull @PositiveOrZero Integer initialStock,
        ProductStatus status,
        String image,
        @PositiveOrZero Integer lowStock,
        Long categoryId) {
}
