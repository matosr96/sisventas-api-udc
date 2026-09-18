package com.api.sisventas.businessLogic.category;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCategories {

    private final CategoryRepository categoryRepository;

    public ListCategories(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> execute(int page, int limit) {
        return PaginatedResponse.from(
                categoryRepository.findAll(Pagination.of(page, limit, "createdAt")),
                CategoryResponse::from);
    }
}
