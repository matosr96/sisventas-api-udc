package com.api.sisventas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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

    /** HS384 exige una clave de al menos 384 bits: 48 bytes. */
    private static final int MIN_SECRET_LENGTH = 48;

    /**
     * Valor que arrastraba el repositorio como ejemplo. Está en un repositorio público,
     * así que firmar con él permitiría a cualquiera forjar tokens: se rechaza de plano.
     */
    private static final String LEAKED_SAMPLE_SECRET = "cambia_este_secreto_por_uno_de_al_menos_32_caracteres";

    private final String secret;
    private final long expiration;

    public JwtGenerator(@Value("${app.jwt.secret}") String secret,
                        @Value("${app.jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * El secreto no tiene valor por defecto: sin él la aplicación no debe arrancar. Aquí se
     * rechaza además un secreto demasiado corto o el ejemplo que vivía en el repositorio,
     * para que un despliegue con un secreto inseguro falle al iniciar en vez de correr indefenso.
     */
    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta JWT_SECRET: define un secreto propio para firmar los tokens.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET demasiado corto: se necesitan al menos " + MIN_SECRET_LENGTH + " bytes.");
        }
        if (LEAKED_SAMPLE_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET es el ejemplo público del repositorio: usa un secreto propio y no compartido.");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static final String ROLES_CLAIM = "roles";
    private static final String VERSION_CLAIM = "ver";

    /** La versión sale del principal cuando es {@link AuthenticatedUser}; si no, cero. */
    public String generateToken(Authentication authentication) {
        int version = authentication.getPrincipal() instanceof AuthenticatedUser user ? user.getTokenVersion() : 0;
        Date now = new Date();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(ROLES_CLAIM, roles)
                .claim(VERSION_CLAIM, version)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(signingKey())
                .compact();
    }

    public String usernameOf(String token) {
        return claimsOf(token).getSubject();
    }

    /** Tokens anteriores a la versión no llevan la reclamación: cuentan como versión cero. */
    public int versionOf(String token) {
        Integer version = claimsOf(token).get(VERSION_CLAIM, Integer.class);
        return version == null ? 0 : version;
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
