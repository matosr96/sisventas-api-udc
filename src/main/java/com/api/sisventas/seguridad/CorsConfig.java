package com.api.sisventas.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/auth/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .exposedHeaders("*");

                registry.addMapping("/api/v1/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .exposedHeaders("*");

                registry.addMapping("/register")
                        .allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }
}
