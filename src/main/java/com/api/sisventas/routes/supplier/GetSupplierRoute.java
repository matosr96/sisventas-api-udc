package com.api.sisventas.routes.supplier;

import com.api.sisventas.businessLogic.supplier.GetSupplier;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Suppliers")
public class GetSupplierRoute {

    private final GetSupplier getSupplier;

    public GetSupplierRoute(GetSupplier getSupplier) {
        this.getSupplier = getSupplier;
    }

    @Operation(summary = "Get supplier", description = "Returns a supplier by id")
    @GetMapping("/api/v1/suppliers/{id}")
    public SupplierResponse handle(@PathVariable Long id) {
        return getSupplier.execute(id);
    }
}
