package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.ListSales;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.PaymentMethod;
import com.api.sisventas.models.dtos.sale.SaleResponse;

import java.time.Instant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class ListSalesRoute {

    private final ListSales listSales;

    public ListSalesRoute(ListSales listSales) {
        this.listSales = listSales;
    }

    @Operation(summary = "List sales",
            description = "Filters: from, to (ISO instants), userId, paymentMethod, search"
                    + " (number/customer); sort: date|total|number")
    @GetMapping("/api/v1/sales")
    public PaginatedResponse<SaleResponse> handle(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listSales.execute(from, to, userId, paymentMethod, search, sort, dir, page, limit);
    }
}
