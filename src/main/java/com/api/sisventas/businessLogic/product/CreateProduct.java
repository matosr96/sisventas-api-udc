package com.api.sisventas.businessLogic.product;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.CategoryRepository;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
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
    private final RecordStockMovement recordStockMovement;

    public CreateProduct(ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         GetAuthenticatedUser getAuthenticatedUser,
                         RecordStockMovement recordStockMovement) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
        this.recordStockMovement = recordStockMovement;
    }

    @Transactional
    public ProductResponse execute(CreateProductRequest request) {
        User creator = getAuthenticatedUser.execute();
        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        // El stock nace en cero y entra por el libro, como cualquier otra variación.
        product.setCurrentStock(0);
        product.setInitialStock(request.initialStock());
        product.setStatus(request.status() == null ? ProductStatus.ACTIVE : request.status());
        product.setImage(request.image());
        product.setLowStock(request.lowStock());
        product.setCreatedBy(creator);
        if (request.categoryId() != null) {
            product.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new DomainError(ErrorCodes.CATEGORY_NOT_FOUND)));
        }
        Product saved = save(product);
        if (request.initialStock() > 0) {
            recordStockMovement.execute(saved, StockMovementType.INITIAL, request.initialStock(), null, null, creator);
        }
        return ProductResponse.from(saved);
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
