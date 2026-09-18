package com.api.sisventas.models.dtos.sale;

import com.api.sisventas.models.Sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        String saleNumber,
        Instant saleDate,
        BigDecimal total,
        Long userId,
        List<SaleItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getSaleDate(),
                sale.getTotal(),
                sale.getUser().getId(),
                sale.getItems().stream().map(SaleItemResponse::from).toList(),
                sale.getCreatedAt(),
                sale.getUpdatedAt());
    }
}
