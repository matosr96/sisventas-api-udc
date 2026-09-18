package com.api.sisventas.businessLogic.category;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.models.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCategory {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DeleteCategory(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /** Una categoría con productos no se borra: dejaría el catálogo sin clasificar. */
    @Transactional
    public void execute(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.CATEGORY_NOT_FOUND));
        if (productRepository.existsByCategoryId(id)) {
            throw new DomainError(ErrorCodes.CATEGORY_IN_USE);
        }
        categoryRepository.delete(category);
    }
}
