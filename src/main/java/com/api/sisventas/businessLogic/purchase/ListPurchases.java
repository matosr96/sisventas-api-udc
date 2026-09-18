package com.api.sisventas.businessLogic.purchase;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Compras por rango de fechas, proveedor y número; orden por fecha o total. */
@Service
public class ListPurchases {

    private static final Map<String, String> SORTS = Map.of(
            "date", "purchaseDate", "total", "total", "number", "purchaseNumber");
    private static final String DEFAULT_SORT = "purchaseDate";

    private final PurchaseRepository repository;

    public ListPurchases(PurchaseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<PurchaseResponse> execute(Instant from, Instant to, Long supplierId, String search,
                                                    String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.between("purchaseDate", from, to),
                                Filters.relationId("supplier", supplierId),
                                Filters.contains(search, "purchaseNumber")),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                PurchaseResponse::from);
    }
}
