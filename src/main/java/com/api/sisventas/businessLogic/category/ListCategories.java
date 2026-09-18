package com.api.sisventas.businessLogic.category;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Categorías por nombre; orden por nombre o fecha de alta. */
@Service
public class ListCategories {

    private static final Map<String, String> SORTS = Map.of("name", "name", "createdAt", "createdAt");
    private static final String DEFAULT_SORT = "createdAt";

    private final CategoryRepository repository;

    public ListCategories(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> execute(String search, String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.contains(search, "name")),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                CategoryResponse::from);
    }
}
