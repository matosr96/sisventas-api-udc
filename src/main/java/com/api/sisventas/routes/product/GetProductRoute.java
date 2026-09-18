package com.api.sisventas.routes.product;

import com.api.sisventas.businessLogic.product.GetProduct;
import com.api.sisventas.models.dtos.product.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products")
public class GetProductRoute {

    private final GetProduct getProduct;

    public GetProductRoute(GetProduct getProduct) {
        this.getProduct = getProduct;
    }

    @Operation(summary = "Obtener producto", description = "Devuelve un producto por su id")
    @GetMapping("/api/v1/products/{id}")
    public ProductResponse handle(@PathVariable Long id) {
        return getProduct.execute(id);
    }
}
