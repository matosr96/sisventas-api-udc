package com.api.sisventas.common;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Catálogo único de códigos de error de dominio.
 *
 * Todo error que la API devuelve al cliente es uno de estos códigos, y aquí vive la
 * única traducción de código a estado HTTP. Añadir un código implica añadirlo también
 * a la tabla del README.
 */
public final class ErrorCodes {

    public static final String PRODUCT_NOT_FOUND = "601";
    public static final String CATEGORY_NOT_FOUND = "602";
    public static final String SALE_NOT_FOUND = "603";
    public static final String USER_NOT_FOUND = "604";
    public static final String SUPPLIER_NOT_FOUND = "605";
    public static final String PURCHASE_NOT_FOUND = "606";

    public static final String USERNAME_ALREADY_EXISTS = "610";
    public static final String INVALID_CREDENTIALS = "611";
    public static final String DEFAULT_ROLE_MISSING = "612";
    public static final String FORBIDDEN = "613";
    public static final String CANNOT_MODIFY_SELF = "614";
    public static final String INVALID_CURRENT_PASSWORD = "615";
    public static final String ROLE_NOT_FOUND = "616";

    public static final String CATEGORY_IN_USE = "620";
    public static final String INSUFFICIENT_STOCK = "621";
    public static final String SKU_ALREADY_EXISTS = "622";
    public static final String CATEGORY_NAME_ALREADY_EXISTS = "623";
    public static final String SUPPLIER_NAME_ALREADY_EXISTS = "624";
    public static final String SUPPLIER_INACTIVE = "625";

    public static final String INVALID_PAGINATION = "630";
    public static final String INVALID_REQUEST = "631";

    public static final String TOO_MANY_REQUESTS = "640";

    public static final String INTEGRITY_VIOLATION = "690";
    public static final String INTERNAL_ERROR = "699";

    private static final Map<String, HttpStatus> STATUSES = Map.ofEntries(
            Map.entry(PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(SALE_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(USER_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(PURCHASE_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(USERNAME_ALREADY_EXISTS, HttpStatus.CONFLICT),
            Map.entry(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED),
            Map.entry(DEFAULT_ROLE_MISSING, HttpStatus.INTERNAL_SERVER_ERROR),
            Map.entry(FORBIDDEN, HttpStatus.FORBIDDEN),
            Map.entry(CANNOT_MODIFY_SELF, HttpStatus.CONFLICT),
            Map.entry(INVALID_CURRENT_PASSWORD, HttpStatus.BAD_REQUEST),
            Map.entry(ROLE_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR),
            Map.entry(CATEGORY_IN_USE, HttpStatus.CONFLICT),
            Map.entry(INSUFFICIENT_STOCK, HttpStatus.CONFLICT),
            Map.entry(SKU_ALREADY_EXISTS, HttpStatus.CONFLICT),
            Map.entry(CATEGORY_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT),
            Map.entry(SUPPLIER_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT),
            Map.entry(SUPPLIER_INACTIVE, HttpStatus.CONFLICT),
            Map.entry(INVALID_PAGINATION, HttpStatus.BAD_REQUEST),
            Map.entry(INVALID_REQUEST, HttpStatus.BAD_REQUEST),
            Map.entry(TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS),
            Map.entry(INTEGRITY_VIOLATION, HttpStatus.CONFLICT),
            Map.entry(INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));

    private ErrorCodes() {
    }

    public static HttpStatus statusOf(String code) {
        return STATUSES.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
