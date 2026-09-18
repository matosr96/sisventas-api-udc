package com.api.sisventas.routes.supplier;

import com.api.sisventas.businessLogic.supplier.DeleteSupplier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Suppliers")
public class DeleteSupplierRoute {

    private final DeleteSupplier deleteSupplier;

    public DeleteSupplierRoute(DeleteSupplier deleteSupplier) {
        this.deleteSupplier = deleteSupplier;
    }

    @Operation(summary = "Eliminar proveedor", description = "Elimina un proveedor, o lo desactiva si tiene compras")
    @DeleteMapping("/api/v1/suppliers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable Long id) {
        deleteSupplier.execute(id);
    }
}
