package com.api.sisventas.models.dtos.product;

import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        Integer currentStock,
        Integer initialStock,
        ProductStatus status,
        String image,
        Integer lowStock,
        Long categoryId,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPurchasePrice(),
                product.getSalePrice(),
                product.getCurrentStock(),
                product.getInitialStock(),
                product.getStatus(),
                product.getImage(),
                product.getLowStock(),
                product.getCategory() == null ? null : product.getCategory().getId(),
                product.getCreatedBy() == null ? null : product.getCreatedBy().getId(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
