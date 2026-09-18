package com.api.sisventas.businessLogic.settings;

import com.api.sisventas.models.dtos.settings.SettingsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Configuración pública del negocio. Vive en propiedades: cambiarla es redesplegar, no una pantalla. */
@Service
public class GetSettings {

    private final SettingsResponse settings;

    public GetSettings(@Value("${app.invoice.business-name}") String businessName,
                       @Value("${app.invoice.currency}") String currency,
                       @Value("${app.sales.tax-rate}") BigDecimal taxRate) {
        this.settings = new SettingsResponse(businessName, currency, taxRate);
    }

    public SettingsResponse execute() {
        return settings;
    }
}
