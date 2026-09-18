package com.api.sisventas.businessLogic.product;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.product.UpdateProductRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProduct {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public UpdateProduct(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /** Actualización parcial: un campo nulo significa "no tocar". */
    @Transactional
    public ProductResponse execute(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.PRODUCT_NOT_FOUND));
        applyDetails(product, request);
        applyStock(product, request);
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

    private void applyDetails(Product product, UpdateProductRequest request) {
        if (request.sku() != null) {
            product.setSku(request.sku());
        }
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.purchasePrice() != null) {
            product.setPurchasePrice(request.purchasePrice());
        }
        if (request.salePrice() != null) {
            product.setSalePrice(request.salePrice());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if (request.image() != null) {
            product.setImage(request.image());
        }
    }

    /** El stock no se toca aquí: solo cambia por compra, venta, anulación o ajuste, con asiento. */
    private void applyStock(Product product, UpdateProductRequest request) {
        if (request.lowStock() != null) {
            product.setLowStock(request.lowStock());
        }
        if (request.categoryId() != null) {
            product.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new DomainError(ErrorCodes.CATEGORY_NOT_FOUND)));
        }
    }
}
