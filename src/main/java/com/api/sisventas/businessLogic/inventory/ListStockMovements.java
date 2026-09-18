package com.api.sisventas.businessLogic.inventory;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.StockMovementRepository;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Libro mayor de un producto: cada asiento con el saldo que dejó. */
@Service
public class ListStockMovements {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ListStockMovements(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<StockMovementResponse> execute(Long productId, int page, int limit) {
        if (!productRepository.existsById(productId)) {
            throw new DomainError(ErrorCodes.PRODUCT_NOT_FOUND);
        }
        return PaginatedResponse.from(
                stockMovementRepository.findByProductId(productId, Pagination.of(page, limit, "createdAt")),
                StockMovementResponse::from);
    }
}
