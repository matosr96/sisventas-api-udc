package com.api.sisventas.models.dtos.settings;

import java.math.BigDecimal;

/** Lo que el panel necesita saber del negocio para pintar dinero y cobrar: nombre, moneda y tasa de impuesto. */
public record SettingsResponse(String businessName, String currency, BigDecimal taxRate) {
}
