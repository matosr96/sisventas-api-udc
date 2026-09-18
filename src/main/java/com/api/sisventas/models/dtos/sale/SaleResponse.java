package com.api.sisventas.models.dtos.sale;

import com.api.sisventas.models.PaymentMethod;
import com.api.sisventas.models.Sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        String saleNumber,
        Instant saleDate,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal taxRate,
        BigDecimal tax,
        BigDecimal total,
        PaymentMethod paymentMethod,
        BigDecimal amountPaid,
        BigDecimal changeAmount,
        String customerName,
        Long userId,
        String userName,
        List<SaleItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getSaleDate(),
                sale.getSubtotal(),
                sale.getDiscount(),
                sale.getTaxRate(),
                sale.getTax(),
                sale.getTotal(),
                sale.getPaymentMethod(),
                sale.getAmountPaid(),
                sale.getChangeAmount(),
                sale.getCustomerName(),
                sale.getUser().getId(),
                sale.getUser().getFirstName() + " " + sale.getUser().getLastName(),
                sale.getItems().stream().map(SaleItemResponse::from).toList(),
                sale.getCreatedAt(),
                sale.getUpdatedAt());
    }
}
