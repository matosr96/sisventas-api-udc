package com.api.sisventas.routes.supplier;

import com.api.sisventas.businessLogic.supplier.CreateSupplier;
import com.api.sisventas.models.dtos.supplier.CreateSupplierRequest;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Suppliers")
public class CreateSupplierRoute {

    private final CreateSupplier createSupplier;

    public CreateSupplierRoute(CreateSupplier createSupplier) {
        this.createSupplier = createSupplier;
    }

    @Operation(summary = "Crear proveedor", description = "Da de alta un proveedor")
    @PostMapping("/api/v1/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse handle(@Valid @RequestBody CreateSupplierRequest request) {
        return createSupplier.execute(request);
    }
}
