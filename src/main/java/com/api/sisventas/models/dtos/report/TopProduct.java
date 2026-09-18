package com.api.sisventas.models.dtos.report;

import java.math.BigDecimal;

/** Unidades netas (vendidas menos devueltas), importe y margen estimado con el último costo conocido. */
public record TopProduct(Long productId, String sku, String name, long quantity, BigDecimal total, BigDecimal margin) {
}
