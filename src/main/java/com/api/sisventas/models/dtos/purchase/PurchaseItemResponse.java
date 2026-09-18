package com.api.sisventas.models.dtos.purchase;

import com.api.sisventas.models.PurchaseItem;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal subtotal) {

    public static PurchaseItemResponse from(PurchaseItem item) {
        return new PurchaseItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getSubtotal());
    }
}
