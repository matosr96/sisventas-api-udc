package com.api.sisventas.businessLogic.product;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.dtos.product.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Catálogo por SKU o nombre, estado, categoría y solo-stock-bajo; orden por nombre, SKU, precio o stock. */
@Service
public class ListProducts {

    private static final Map<String, String> SORTS = Map.of(
            "name", "name", "sku", "sku", "price", "salePrice", "stock", "currentStock", "createdAt", "createdAt");
    private static final String DEFAULT_SORT = "createdAt";

    private final ProductRepository repository;

    public ListProducts(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> execute(String search, ProductStatus status, Long categoryId,
                                                   Boolean lowStock, String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.contains(search, "sku", "name"),
                                Filters.equal("status", status),
                                Filters.relationId("category", categoryId),
                                Filters.lowStock(lowStock)),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                ProductResponse::from);
    }
}
