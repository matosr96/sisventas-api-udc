package com.api.sisventas.routes.inventory;

import com.api.sisventas.businessLogic.inventory.AdjustStock;
import com.api.sisventas.models.dtos.inventory.AdjustStockRequest;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inventory")
public class AdjustStockRoute {

    private final AdjustStock adjustStock;

    public AdjustStockRoute(AdjustStock adjustStock) {
        this.adjustStock = adjustStock;
    }

    @Operation(summary = "Adjust stock",
            description = "Adds or removes units with a mandatory reason, leaving a ledger entry")
    @PostMapping("/api/v1/products/{id}/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse handle(@PathVariable Long id, @Valid @RequestBody AdjustStockRequest request) {
        return adjustStock.execute(id, request);
    }
}
