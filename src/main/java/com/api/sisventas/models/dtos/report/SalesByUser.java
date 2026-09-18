package com.api.sisventas.models.dtos.report;

import java.math.BigDecimal;

public record SalesByUser(Long userId, String userName, long count, BigDecimal total) {
}
