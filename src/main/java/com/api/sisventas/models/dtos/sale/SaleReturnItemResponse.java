package com.api.sisventas.models.dtos.sale;

import com.api.sisventas.models.SaleReturnItem;

import java.math.BigDecimal;

public record SaleReturnItemResponse(
        Long id,
        Long saleItemId,
        Long productId,
        String productSku,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {

    public static SaleReturnItemResponse from(SaleReturnItem item) {
        return new SaleReturnItemResponse(
                item.getId(),
                item.getSaleItem().getId(),
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal());
    }
}
