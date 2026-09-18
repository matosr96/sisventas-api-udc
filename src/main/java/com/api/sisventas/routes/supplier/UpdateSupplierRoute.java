package com.api.sisventas.routes.supplier;

import com.api.sisventas.businessLogic.supplier.UpdateSupplier;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import com.api.sisventas.models.dtos.supplier.UpdateSupplierRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Suppliers")
public class UpdateSupplierRoute {

    private final UpdateSupplier updateSupplier;

    public UpdateSupplierRoute(UpdateSupplier updateSupplier) {
        this.updateSupplier = updateSupplier;
    }

    @Operation(summary = "Actualizar proveedor", description = "Actualización parcial: solo los campos presentes")
    @PutMapping("/api/v1/suppliers/{id}")
    public SupplierResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest request) {
        return updateSupplier.execute(id, request);
    }
}
