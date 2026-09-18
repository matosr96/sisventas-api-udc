package com.api.sisventas.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** La traducción de ?page&limit&sort&dir: base 1, tope 100, lista blanca de orden. Sin Spring. */
class PaginationTest {

    private static final Map<String, String> SORTS = Map.of("name", "name", "price", "salePrice");

    @Test
    void translatesPublicParamsAndFallsBackToTheDefaultSort() {
        Pageable page = Pagination.of(3, 25, "price", "asc", SORTS, "createdAt");
        assertEquals(2, page.getPageNumber());
        assertEquals(25, page.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "salePrice"), page.getSort());

        Pageable unknown = Pagination.of(1, 10, "hacker", "sideways", SORTS, "createdAt");
        assertEquals(Sort.by(Sort.Direction.DESC, "createdAt"), unknown.getSort(), "nombre desconocido y dir rara: valores por defecto");
        assertEquals(Sort.by(Sort.Direction.DESC, "createdAt"), Pagination.of(1, 10, null, null, SORTS, "createdAt").getSort());
    }

    @Test
    void rejectsPagesBelowOneAndLimitsOutOfRange() {
        for (int[] bad : new int[][] {{0, 10}, {1, 0}, {1, 101}, {-1, 10}}) {
            DomainError error = assertThrows(DomainError.class, () -> Pagination.of(bad[0], bad[1], "createdAt"));
            assertEquals(ErrorCodes.INVALID_PAGINATION, error.code());
        }
        assertEquals(100, Pagination.of(1, 100, "createdAt").getPageSize());
    }
}
