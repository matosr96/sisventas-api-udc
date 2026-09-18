package com.api.sisventas.routes.product;

import com.api.sisventas.businessLogic.product.UpdateProduct;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.product.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products")
public class UpdateProductRoute {

    private final UpdateProduct updateProduct;

    public UpdateProductRoute(UpdateProduct updateProduct) {
        this.updateProduct = updateProduct;
    }

    @Operation(summary = "Update product", description = "Partial update: only the fields present")
    @PutMapping("/api/v1/products/{id}")
    public ProductResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return updateProduct.execute(id, request);
    }
}
