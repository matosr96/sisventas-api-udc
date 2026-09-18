package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.CreateSale;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class CreateSaleRoute {

    private final CreateSale createSale;

    public CreateSaleRoute(CreateSale createSale) {
        this.createSale = createSale;
    }

    @Operation(summary = "Register sale",
            description = "Registers a sale: freezes unit prices, decrements stock and computes the total")
    @PostMapping("/api/v1/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse handle(@Valid @RequestBody CreateSaleRequest request) {
        return createSale.execute(request);
    }
}
