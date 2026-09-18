package com.api.sisventas.server;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    private final String devUrl;
    private final String prodUrl;

    public OpenApiConfig(@Value("${app.openapi.dev-url}") String devUrl,
                         @Value("${app.openapi.prod-url}") String prodUrl) {
        this.devUrl = devUrl;
        this.prodUrl = prodUrl;
    }

    @Bean
    public OpenAPI openApi() {
        Info info = new Info()
                .title("API de Sistema de Ventas")
                .version("1.0")
                .description("Errores de dominio: ver la tabla de códigos en el README.")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"));

        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(info)
                .servers(List.of(
                        new Server().url(devUrl).description("Desarrollo"),
                        new Server().url(prodUrl).description("Producción")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, bearer))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
