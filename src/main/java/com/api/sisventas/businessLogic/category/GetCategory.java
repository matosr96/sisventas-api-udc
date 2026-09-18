package com.api.sisventas.businessLogic.category;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCategory {

    private final CategoryRepository categoryRepository;

    public GetCategory(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public CategoryResponse execute(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::from)
                .orElseThrow(() -> new DomainError(ErrorCodes.CATEGORY_NOT_FOUND));
    }
}
