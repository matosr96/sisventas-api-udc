package com.api.sisventas.common;

/**
 * Error de negocio identificado por un código de {@link ErrorCodes}.
 *
 * El mensaje de la excepción ES el código: lo que el cliente recibe en
 * {@code {"message": "601"}}. Nunca lleva texto libre ni datos del usuario.
 */
public class DomainError extends RuntimeException {

    public DomainError(String code) {
        super(code);
    }

    public String code() {
        return getMessage();
    }
}
