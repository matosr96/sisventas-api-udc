package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.PaymentMethod;
import com.api.sisventas.models.dtos.sale.SaleResponse;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Ventas por rango de fechas, vendedor, método de pago y número o cliente; orden por fecha o total. */
@Service
public class ListSales {

    private static final Map<String, String> SORTS = Map.of(
            "date", "saleDate", "total", "total", "number", "saleNumber");
    private static final String DEFAULT_SORT = "saleDate";

    private final SaleRepository repository;

    public ListSales(SaleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<SaleResponse> execute(Instant from, Instant to, Long userId,
                                                PaymentMethod paymentMethod, String search,
                                                String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.between("saleDate", from, to),
                                Filters.relationId("user", userId),
                                Filters.equal("paymentMethod", paymentMethod),
                                Filters.contains(search, "saleNumber", "customerName")),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                SaleResponse::from);
    }
}
