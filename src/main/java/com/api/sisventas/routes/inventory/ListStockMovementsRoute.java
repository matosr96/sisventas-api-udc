package com.api.sisventas.routes.inventory;

import com.api.sisventas.businessLogic.inventory.ListStockMovements;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inventory")
public class ListStockMovementsRoute {

    private final ListStockMovements listStockMovements;

    public ListStockMovementsRoute(ListStockMovements listStockMovements) {
        this.listStockMovements = listStockMovements;
    }

    @Operation(summary = "Product stock ledger",
            description = "Paginated entries, newest first")
    @GetMapping("/api/v1/products/{id}/movements")
    public PaginatedResponse<StockMovementResponse> handle(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listStockMovements.execute(id, page, limit);
    }
}
