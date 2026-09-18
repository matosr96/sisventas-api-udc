package com.api.sisventas.models.dtos.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Cierre de caja de un día (y opcionalmente un vendedor): cobrado por método, devuelto y neto. */
public record CashClosingResponse(
        Instant from,
        Instant to,
        Long userId,
        long saleCount,
        BigDecimal total,
        BigDecimal returned,
        BigDecimal net,
        List<PaymentBreakdown> byPaymentMethod) {
}
