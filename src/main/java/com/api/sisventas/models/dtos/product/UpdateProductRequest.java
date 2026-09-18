package com.api.sisventas.models.dtos.product;

import com.api.sisventas.models.ProductStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Actualización parcial: solo se aplican los campos presentes (no nulos).
 * El stock no está aquí a propósito: solo se mueve por compra, venta, anulación o ajuste.
 */
public record UpdateProductRequest(
        @Size(max = 64) String sku,
        @Size(max = 120) String name,
        @PositiveOrZero BigDecimal purchasePrice,
        @PositiveOrZero BigDecimal salePrice,
        ProductStatus status,
        String image,
        @PositiveOrZero Integer lowStock,
        Long categoryId) {
}
