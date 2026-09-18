package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.ListSales;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.dtos.sale.SaleResponse;
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

    @Operation(summary = "Listar ventas", description = "Listado paginado { count, page, pages, items }")
    @GetMapping("/api/v1/sales")
    public PaginatedResponse<SaleResponse> handle(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listSales.execute(page, limit);
    }
}
