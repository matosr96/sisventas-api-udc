package com.api.sisventas.routes.purchase;

import com.api.sisventas.businessLogic.purchase.DeletePurchase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Purchases")
public class DeletePurchaseRoute {

    private final DeletePurchase deletePurchase;

    public DeletePurchaseRoute(DeletePurchase deletePurchase) {
        this.deletePurchase = deletePurchase;
    }

    @Operation(summary = "Anular compra", description = "Anula la compra y retira del stock lo que había sumado")
    @DeleteMapping("/api/v1/purchases/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable Long id) {
        deletePurchase.execute(id);
    }
}
