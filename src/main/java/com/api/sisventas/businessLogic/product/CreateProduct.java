package com.api.sisventas.businessLogic.product;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProduct {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public CreateProduct(ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         GetAuthenticatedUser getAuthenticatedUser) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public ProductResponse execute(CreateProductRequest request) {
        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setCurrentStock(request.currentStock());
        product.setInitialStock(request.initialStock() == null ? request.currentStock() : request.initialStock());
        product.setStatus(request.status() == null ? ProductStatus.ACTIVE : request.status());
        product.setImage(request.image());
        product.setLowStock(request.lowStock());
        product.setCreatedBy(getAuthenticatedUser.execute());
        if (request.categoryId() != null) {
            product.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new DomainError(ErrorCodes.CATEGORY_NOT_FOUND)));
        }
        return ProductResponse.from(save(product));
    }

    /** El unique de {@code products.sku} es la única verificación: no se consulta antes. */
    private Product save(Product product) {
        try {
            return productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException error) {
            if (SqlErrors.isUniqueViolation(error)) {
                throw new DomainError(ErrorCodes.SKU_ALREADY_EXISTS);
            }
            throw error;
        }
    }
}
