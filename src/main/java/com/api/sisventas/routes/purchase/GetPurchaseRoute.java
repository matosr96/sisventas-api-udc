package com.api.sisventas.routes.purchase;

import com.api.sisventas.businessLogic.purchase.GetPurchase;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Purchases")
public class GetPurchaseRoute {

    private final GetPurchase getPurchase;

    public GetPurchaseRoute(GetPurchase getPurchase) {
        this.getPurchase = getPurchase;
    }

    @Operation(summary = "Get purchase", description = "Returns a purchase with its line items")
    @GetMapping("/api/v1/purchases/{id}")
    public PurchaseResponse handle(@PathVariable Long id) {
        return getPurchase.execute(id);
    }
}
