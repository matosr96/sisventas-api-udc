package com.api.sisventas.businessLogic.category;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.models.Category;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import com.api.sisventas.models.dtos.category.CreateCategoryRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCategory {

    private final CategoryRepository categoryRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public CreateCategory(CategoryRepository categoryRepository, GetAuthenticatedUser getAuthenticatedUser) {
        this.categoryRepository = categoryRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public CategoryResponse execute(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setIcon(request.icon());
        category.setCreatedBy(getAuthenticatedUser.execute());
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
