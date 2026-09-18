package com.api.sisventas.models.dtos.report;

import java.math.BigDecimal;

/** Cifras de Inicio: hoy frente a ayer, el mes en compras y el estado del inventario. */
public record SummaryResponse(
        long todaySaleCount,
        BigDecimal todayTotal,
        long yesterdaySaleCount,
        BigDecimal yesterdayTotal,
        long monthPurchaseCount,
        BigDecimal monthPurchaseTotal,
        long activeProductCount,
        long lowStockCount,
        BigDecimal inventoryValue) {
}
