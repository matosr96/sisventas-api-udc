package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.GetSale;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class GetSaleRoute {

    private final GetSale getSale;

    public GetSaleRoute(GetSale getSale) {
        this.getSale = getSale;
    }

    @Operation(summary = "Get sale", description = "Returns a sale with its line items")
    @GetMapping("/api/v1/sales/{id}")
    public SaleResponse handle(@PathVariable Long id) {
        return getSale.execute(id);
    }
}
