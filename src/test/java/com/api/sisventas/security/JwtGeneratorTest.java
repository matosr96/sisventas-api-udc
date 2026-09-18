package com.api.sisventas.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Firma, versión de token y rechazo de secretos inseguros. Sin Spring. */
class JwtGeneratorTest {

    private static final String SECRET = "a-test-secret-that-is-comfortably-longer-than-forty-eight-bytes";
    private static final long ONE_HOUR = 3_600_000L;

    private final JwtGenerator generator = new JwtGenerator(SECRET, ONE_HOUR);

    @Test
    void tokenCarriesSubjectAndTheAccountTokenVersion() {
        var authorities = List.of(new SimpleGrantedAuthority("USER"));
        AuthenticatedUser principal = new AuthenticatedUser("ana", "hash", true, authorities, 3);
        String token = generator.generateToken(new UsernamePasswordAuthenticationToken(principal, null, authorities));
        assertTrue(generator.isValid(token));
        assertEquals("ana", generator.usernameOf(token));
        assertEquals(3, generator.versionOf(token));

        String plain = generator.generateToken(new UsernamePasswordAuthenticationToken("bob", null, authorities));
        assertEquals(0, generator.versionOf(plain), "sin principal propio, versión cero");
    }

    @Test
    void tamperedOrForeignTokensAreInvalidNotErrors() {
        String token = generator.generateToken(new UsernamePasswordAuthenticationToken("ana", null, List.of()));
        assertFalse(generator.isValid(token.substring(0, token.length() - 3) + "abc"));
        assertFalse(generator.isValid("not.a.jwt"));
        JwtGenerator other = new JwtGenerator(SECRET + "-other-key-for-another-deployment", ONE_HOUR);
        assertFalse(other.isValid(token), "firmado con otra clave");
    }

    @Test
    void refusesMissingShortOrLeakedSecrets() {
        assertThrows(IllegalStateException.class, () -> new JwtGenerator(" ", ONE_HOUR).validateSecret());
        assertThrows(IllegalStateException.class, () -> new JwtGenerator("short", ONE_HOUR).validateSecret());
        assertThrows(IllegalStateException.class,
                () -> new JwtGenerator("cambia_este_secreto_por_uno_de_al_menos_32_caracteres", ONE_HOUR).validateSecret());
        generator.validateSecret();
    }
}
