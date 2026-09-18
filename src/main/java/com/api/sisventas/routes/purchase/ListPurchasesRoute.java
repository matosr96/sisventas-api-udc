package com.api.sisventas.routes.purchase;

import com.api.sisventas.businessLogic.purchase.ListPurchases;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Purchases")
public class ListPurchasesRoute {

    private final ListPurchases listPurchases;

    public ListPurchasesRoute(ListPurchases listPurchases) {
        this.listPurchases = listPurchases;
    }

    @Operation(summary = "List purchases", description = "Paginated list { count, page, pages, items }")
    @GetMapping("/api/v1/purchases")
    public PaginatedResponse<PurchaseResponse> handle(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listPurchases.execute(page, limit);
    }
}
