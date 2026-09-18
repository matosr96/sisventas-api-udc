package com.api.sisventas.routes.purchase;

import com.api.sisventas.businessLogic.purchase.CreatePurchase;
import com.api.sisventas.models.dtos.purchase.CreatePurchaseRequest;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Purchases")
public class CreatePurchaseRoute {

    private final CreatePurchase createPurchase;

    public CreatePurchaseRoute(CreatePurchase createPurchase) {
        this.createPurchase = createPurchase;
    }

    @Operation(summary = "Registrar compra",
            description = "Registra una compra a proveedor: suma stock y congela el costo")
    @PostMapping("/api/v1/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse handle(@Valid @RequestBody CreatePurchaseRequest request) {
        return createPurchase.execute(request);
    }
}
