package com.api.sisventas.routes.category;

import com.api.sisventas.businessLogic.category.CreateCategory;
import com.api.sisventas.models.dtos.category.CreateCategoryRequest;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Categories")
public class CreateCategoryRoute {

    private final CreateCategory createCategory;

    public CreateCategoryRoute(CreateCategory createCategory) {
        this.createCategory = createCategory;
    }

    @Operation(summary = "Create category", description = "Creates a category")
    @PostMapping("/api/v1/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse handle(@Valid @RequestBody CreateCategoryRequest request) {
        return createCategory.execute(request);
    }
}
