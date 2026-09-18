package com.api.sisventas.models.dtos.sale;

import com.api.sisventas.models.SaleItem;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        Integer quantity,
        Integer returnedQuantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {

    public static SaleItemResponse from(SaleItem item) {
        return new SaleItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getReturnedQuantity(),
                item.getUnitPrice(),
                item.getSubtotal());
    }
}
