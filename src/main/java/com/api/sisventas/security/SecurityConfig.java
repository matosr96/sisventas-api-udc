package com.api.sisventas.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Matriz de autorización: el ÚNICO sitio donde se decide quién puede llamar a qué.
 * Ninguna ruta repite reglas de acceso ni usa anotaciones de seguridad por método.
 *
 * Regla: lo que no está en {@link SecurityConstants#PUBLIC_URLS} exige token; las
 * escrituras exigen ADMIN salvo que se liste abajo como escritura de USER.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String allowedOrigins;
    private final String allowedMethods;
    private final String allowedHeaders;

    public SecurityConfig(JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Value("${app.cors.allowed-origins}") String allowedOrigins,
                          @Value("${app.cors.allowed-methods}") String allowedMethods,
                          @Value("${app.cors.allowed-headers}") String allowedHeaders) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.deny()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
                        // Lecturas: cualquier usuario autenticado.
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                        // Ventas: las registra y corrige quien vende.
                        .requestMatchers(HttpMethod.POST, "/api/v1/sales/**")
                        .hasAnyAuthority(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_USER)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/sales/**")
                        .hasAnyAuthority(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_USER)
                        // Catálogo y borrados: solo ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
