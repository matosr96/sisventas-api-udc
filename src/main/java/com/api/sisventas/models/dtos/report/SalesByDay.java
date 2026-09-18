package com.api.sisventas.models.dtos.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesByDay(LocalDate day, long count, BigDecimal total) {
}
