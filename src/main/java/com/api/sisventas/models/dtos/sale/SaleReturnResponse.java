package com.api.sisventas.models.dtos.sale;

import com.api.sisventas.models.SaleReturn;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleReturnResponse(
        Long id,
        String returnNumber,
        Long saleId,
        String saleNumber,
        String reason,
        BigDecimal total,
        Long userId,
        List<SaleReturnItemResponse> items,
        Instant createdAt) {

    public static SaleReturnResponse from(SaleReturn saleReturn) {
        return new SaleReturnResponse(
                saleReturn.getId(),
                saleReturn.getReturnNumber(),
                saleReturn.getSale().getId(),
                saleReturn.getSale().getSaleNumber(),
                saleReturn.getReason(),
                saleReturn.getTotal(),
                saleReturn.getUser().getId(),
                saleReturn.getItems().stream().map(SaleReturnItemResponse::from).toList(),
                saleReturn.getCreatedAt());
    }
}
