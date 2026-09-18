package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.UpdateSale;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import com.api.sisventas.models.dtos.sale.UpdateSaleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class UpdateSaleRoute {

    private final UpdateSale updateSale;

    public UpdateSaleRoute(UpdateSale updateSale) {
        this.updateSale = updateSale;
    }

    @Operation(summary = "Actualizar venta", description = "Solo corrige la fecha: líneas y total son inmutables")
    @PutMapping("/api/v1/sales/{id}")
    public SaleResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateSaleRequest request) {
        return updateSale.execute(id, request);
    }
}
