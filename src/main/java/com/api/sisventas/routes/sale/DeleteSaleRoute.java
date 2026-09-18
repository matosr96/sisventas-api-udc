package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.DeleteSale;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class DeleteSaleRoute {

    private final DeleteSale deleteSale;

    public DeleteSaleRoute(DeleteSale deleteSale) {
        this.deleteSale = deleteSale;
    }

    @Operation(summary = "Eliminar venta", description = "Elimina una venta por su id")
    @DeleteMapping("/api/v1/sales/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable Long id) {
        deleteSale.execute(id);
    }
}
