package com.api.sisventas.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Contrato único de los listados de la API: {@code { count, page, pages, items }}.
 * La página es base 1, tal como la expone el cliente.
 */
public record PaginatedResponse<T>(long count, int page, int pages, List<T> items) {

    public static <E, T> PaginatedResponse<T> from(Page<E> page, Function<E, T> toResponse) {
        return new PaginatedResponse<>(
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getContent().stream().map(toResponse).toList());
    }
}
