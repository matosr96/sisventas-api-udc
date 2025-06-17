package com.api.sisventas.seguridad;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtGenerador {
    private static final Logger logger = LoggerFactory.getLogger(JwtGenerador.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(Authentication authentication) {
        String username = authentication.getName();
        Date tiempoActual = new Date();
        Date expiracionToken = new Date(tiempoActual.getTime() + jwtExpiration);

        // Obtener roles del usuario
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("sub", username);
        claims.put("created", new Date());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(tiempoActual)
                .setExpiration(expiracionToken)
                .signWith(getSigningKey())
                .compact();
    }

    public String obtenerUsernameDeJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Firma JWT inválida: {}", ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Token JWT inválido");
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT mal formado: {}", ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Token JWT mal formado");
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Token JWT expirado");
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Token JWT no soportado");
        } catch (IllegalArgumentException ex) {
            logger.error("Token JWT vacío: {}", ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Token JWT vacío");
        }
    }
}
