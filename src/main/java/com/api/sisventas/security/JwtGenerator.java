package com.api.sisventas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/** Emisión y verificación del JWT. Es el único sitio que firma o lee un token. */
@Component
public class JwtGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(JwtGenerator.class);

    private final String secret;
    private final long expiration;

    public JwtGenerator(@Value("${app.jwt.secret}") String secret,
                        @Value("${app.jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        Date now = new Date();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(signingKey())
                .compact();
    }

    public String usernameOf(String token) {
        return claimsOf(token).getSubject();
    }

    /**
     * Un token inválido no es un error de la aplicación: la petición sigue anónima y la
     * matriz de autorización decide. Por eso devuelve false en vez de lanzar.
     */
    public boolean isValid(String token) {
        try {
            claimsOf(token);
            return true;
        } catch (JwtException | IllegalArgumentException error) {
            LOG.debug("Token JWT rechazado: {}", error.getMessage());
            return false;
        }
    }

    private Claims claimsOf(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
