package com.api.sisventas.server;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * Única traducción de excepción a respuesta HTTP. Gracias a esto ninguna ruta
 * necesita try/catch y todos los errores comparten el mismo contrato:
 * {@code {"message": "<código>"}}.
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(DomainError.class)
    public ResponseEntity<Map<String, String>> onDomainError(DomainError error) {
        return respond(error.code());
    }

    /**
     * Todo lo que el cliente mandó mal es un 631: un DTO que no pasa la validación, un cuerpo que
     * no es JSON o trae un tipo equivocado, un parámetro obligatorio ausente o un {@code {id}} que
     * no es número. Sin esto, un cuerpo malformado terminaba en 500 y se logueaba como error propio.
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, String>> onInvalidRequest(Exception error) {
        LOG.debug("Petición inválida: {}", error.getMessage());
        return respond(ErrorCodes.INVALID_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> onIntegrityViolation(DataIntegrityViolationException error) {
        LOG.warn("Violación de integridad", error);
        return respond(ErrorCodes.INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> onAccessDenied(AccessDeniedException error) {
        return respond(ErrorCodes.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> onUnexpected(Exception error) {
        LOG.error("Error no controlado", error);
        return respond(ErrorCodes.INTERNAL_ERROR);
    }

    private ResponseEntity<Map<String, String>> respond(String code) {
        return ResponseEntity.status(ErrorCodes.statusOf(code)).body(Map.of("message", code));
    }
}
