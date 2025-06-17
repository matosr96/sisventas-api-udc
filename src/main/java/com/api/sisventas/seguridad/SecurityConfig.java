package com.api.sisventas.seguridad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, 
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors()
                .and()
            .csrf()
                .disable()
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeHttpRequests()
                // Endpoints públicos
                .requestMatchers(
                    new AntPathRequestMatcher("/api/v1/auth/**"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/actuator/**")
                ).permitAll()
                // Endpoints de productos
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAuthority("ADMIN")
                // Endpoints de facturas
                .requestMatchers(HttpMethod.GET, "/api/v1/invoices/**").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/invoices/**").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/invoices/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/invoices/**").hasAuthority("ADMIN")
                // Endpoints de usuarios
                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAuthority("ADMIN")
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
                .and()
            .headers()
                .xssProtection()
                .and()
                .contentSecurityPolicy("default-src 'self'")
                .and()
            .frameOptions()
                .deny();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
