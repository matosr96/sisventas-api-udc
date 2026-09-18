package com.api.sisventas.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Traduce los parámetros públicos {@code ?page=&limit=} (página base 1, límite por
 * defecto 10) al {@link Pageable} base 0 de Spring Data. Única conversión del proyecto.
 */
public final class Pagination {

    public static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private Pagination() {
    }

    public static Pageable of(int page, int limit, String sortField) {
        if (page < 1 || limit < 1 || limit > MAX_LIMIT) {
            throw new DomainError(ErrorCodes.INVALID_PAGINATION);
        }
        return PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, sortField));
    }
}
