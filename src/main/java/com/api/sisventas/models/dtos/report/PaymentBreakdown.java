package com.api.sisventas.models.dtos.report;

import com.api.sisventas.models.PaymentMethod;

import java.math.BigDecimal;

public record PaymentBreakdown(PaymentMethod paymentMethod, long count, BigDecimal total) {
}
