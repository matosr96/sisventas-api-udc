package com.api.sisventas.routes.settings;

import com.api.sisventas.businessLogic.settings.GetSettings;
import com.api.sisventas.models.dtos.settings.SettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Settings")
public class GetSettingsRoute {

    private final GetSettings getSettings;

    public GetSettingsRoute(GetSettings getSettings) {
        this.getSettings = getSettings;
    }

    @Operation(summary = "Business settings",
            description = "Business name, currency and tax rate applied to sales")
    @GetMapping("/api/v1/settings")
    public SettingsResponse handle() {
        return getSettings.execute();
    }
}
