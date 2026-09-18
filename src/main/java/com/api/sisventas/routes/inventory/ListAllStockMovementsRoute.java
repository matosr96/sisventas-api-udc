package com.api.sisventas.routes.inventory;

import com.api.sisventas.businessLogic.inventory.ListAllStockMovements;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;

import java.time.Instant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inventory")
public class ListAllStockMovementsRoute {

    private final ListAllStockMovements listAllStockMovements;

    public ListAllStockMovementsRoute(ListAllStockMovements listAllStockMovements) {
        this.listAllStockMovements = listAllStockMovements;
    }

    @Operation(summary = "List all stock movements",
            description = "Global ledger. Filters: productId, type, from, to; sort:"
                    + " createdAt|quantity")
    @GetMapping("/api/v1/inventory/movements")
    public PaginatedResponse<StockMovementResponse> handle(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) StockMovementType type,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listAllStockMovements.execute(productId, type, from, to, sort, dir, page, limit);
    }
}
