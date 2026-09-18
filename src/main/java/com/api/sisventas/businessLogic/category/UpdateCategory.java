package com.api.sisventas.businessLogic.category;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.models.Category;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import com.api.sisventas.models.dtos.category.UpdateCategoryRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCategory {

    private final CategoryRepository categoryRepository;

    public UpdateCategory(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse execute(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.CATEGORY_NOT_FOUND));
        if (request.name() != null) {
            category.setName(request.name());
        }
        if (request.icon() != null) {
            category.setIcon(request.icon());
        }
        try {
            return CategoryResponse.from(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException error) {
            if (SqlErrors.isUniqueViolation(error)) {
                throw new DomainError(ErrorCodes.CATEGORY_NAME_ALREADY_EXISTS);
            }
            throw error;
        }
    }
}
