package com.api.sisventas.businessLogic.inventory;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.StockMovementRepository;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Libro de stock completo, de todos los productos: por producto, tipo y rango de fechas. */
@Service
public class ListAllStockMovements {

    private static final Map<String, String> SORTS = Map.of("createdAt", "createdAt", "quantity", "quantity");
    private static final String DEFAULT_SORT = "createdAt";

    private final StockMovementRepository repository;

    public ListAllStockMovements(StockMovementRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<StockMovementResponse> execute(Long productId, StockMovementType type,
                                                            Instant from, Instant to,
                                                            String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.relationId("product", productId),
                                Filters.equal("type", type),
                                Filters.between("createdAt", from, to)),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                StockMovementResponse::from);
    }
}
