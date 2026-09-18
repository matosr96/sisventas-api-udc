package com.api.sisventas.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Traduce los parámetros públicos {@code ?page=&limit=&sort=&dir=} (página base 1, límite
 * por defecto 10) al {@link Pageable} base 0 de Spring Data. Única conversión del proyecto.
 * El campo de orden se elige de una lista blanca por listado: nunca se pasa tal cual al ORM.
 */
public final class Pagination {

    public static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private Pagination() {
    }

    public static Pageable of(int page, int limit, String sortField) {
        return of(page, limit, sortField, Sort.Direction.DESC);
    }

    public static Pageable of(int page, int limit, String sortField, Sort.Direction direction) {
        if (page < 1 || limit < 1 || limit > MAX_LIMIT) {
            throw new DomainError(ErrorCodes.INVALID_PAGINATION);
        }
        return PageRequest.of(page - 1, limit, Sort.by(direction, sortField));
    }

    /**
     * Resuelve {@code sort}/{@code dir} contra la lista blanca del listado: un nombre
     * desconocido cae al campo por defecto, y {@code dir} solo entiende {@code asc}.
     */
    public static Pageable of(int page, int limit, String sort, String dir,
                              Map<String, String> allowedSorts, String defaultSort) {
        String field = allowedSorts.getOrDefault(sort == null ? "" : sort, defaultSort);
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return of(page, limit, field, direction);
    }
}
