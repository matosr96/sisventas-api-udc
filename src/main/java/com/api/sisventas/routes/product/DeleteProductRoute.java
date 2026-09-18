package com.api.sisventas.routes.product;

import com.api.sisventas.businessLogic.product.DeleteProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products")
public class DeleteProductRoute {

    private final DeleteProduct deleteProduct;

    public DeleteProductRoute(DeleteProduct deleteProduct) {
        this.deleteProduct = deleteProduct;
    }

    @Operation(summary = "Delete product",
            description = "Deletes a product, or deactivates it if it has ledger entries")
    @DeleteMapping("/api/v1/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable Long id) {
        deleteProduct.execute(id);
    }
}
