package com.api.sisventas.routes.product;

import com.api.sisventas.businessLogic.product.CreateProduct;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products")
public class CreateProductRoute {

    private final CreateProduct createProduct;

    public CreateProductRoute(CreateProduct createProduct) {
        this.createProduct = createProduct;
    }

    @Operation(summary = "Create product",
            description = "Creates a product; stock enters the ledger as an INITIAL entry")
    @PostMapping("/api/v1/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse handle(@Valid @RequestBody CreateProductRequest request) {
        return createProduct.execute(request);
    }
}
