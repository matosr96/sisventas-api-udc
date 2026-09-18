package com.api.sisventas.dataSources;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Traduce fallos de constraint de MySQL a algo que la lógica de negocio pueda decidir.
 *
 * Existe para cumplir la regla "nunca consultar antes para verificar lo que un unique ya
 * garantiza": se intenta la escritura y, si la base la rechaza, se mapea a un código de dominio.
 */
public final class SqlErrors {

    /** Código de error de MySQL para entrada duplicada en un índice único. */
    private static final int DUPLICATE_ENTRY = 1062;
    /** SQLState estándar de "unique violation" (H2 en pruebas, PostgreSQL). */
    private static final String UNIQUE_VIOLATION_STATE = "23505";

    private SqlErrors() {
    }

    public static boolean isUniqueViolation(DataIntegrityViolationException error) {
        Throwable cause = error.getMostSpecificCause();
        if (cause instanceof SQLIntegrityConstraintViolationException sqlCause) {
            return sqlCause.getErrorCode() == DUPLICATE_ENTRY || UNIQUE_VIOLATION_STATE.equals(sqlCause.getSQLState());
        }
        return false;
    }
}
