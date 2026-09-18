package com.api.sisventas.models.dtos.purchase;

import com.api.sisventas.models.Purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseResponse(
        Long id,
        String purchaseNumber,
        Instant purchaseDate,
        Long supplierId,
        String supplierName,
        BigDecimal total,
        Long userId,
        List<PurchaseItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {

    public static PurchaseResponse from(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getPurchaseNumber(),
                purchase.getPurchaseDate(),
                purchase.getSupplier().getId(),
                purchase.getSupplier().getName(),
                purchase.getTotal(),
                purchase.getUser().getId(),
                purchase.getItems().stream().map(PurchaseItemResponse::from).toList(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt());
    }
}
