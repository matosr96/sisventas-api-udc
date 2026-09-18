package com.api.sisventas.routes.purchase;

import com.api.sisventas.businessLogic.purchase.UpdatePurchase;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import com.api.sisventas.models.dtos.purchase.UpdatePurchaseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Purchases")
public class UpdatePurchaseRoute {

    private final UpdatePurchase updatePurchase;

    public UpdatePurchaseRoute(UpdatePurchase updatePurchase) {
        this.updatePurchase = updatePurchase;
    }

    @Operation(summary = "Actualizar compra", description = "Solo corrige la fecha: líneas y total son inmutables")
    @PutMapping("/api/v1/purchases/{id}")
    public PurchaseResponse handle(@PathVariable Long id, @Valid @RequestBody UpdatePurchaseRequest request) {
        return updatePurchase.execute(id, request);
    }
}
