package com.api.sisventas.models.dtos.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Informe de ventas de un periodo: totales, serie diaria, por vendedor y productos más vendidos con margen. */
public record SalesReportResponse(
        Instant from,
        Instant to,
        long count,
        BigDecimal total,
        BigDecimal estimatedMargin,
        List<SalesByDay> byDay,
        List<SalesByUser> byUser,
        List<TopProduct> topProducts) {
}
