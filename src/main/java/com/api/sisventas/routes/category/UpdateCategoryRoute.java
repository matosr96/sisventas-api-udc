package com.api.sisventas.routes.category;

import com.api.sisventas.businessLogic.category.UpdateCategory;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import com.api.sisventas.models.dtos.category.UpdateCategoryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Categories")
public class UpdateCategoryRoute {

    private final UpdateCategory updateCategory;

    public UpdateCategoryRoute(UpdateCategory updateCategory) {
        this.updateCategory = updateCategory;
    }

    @Operation(summary = "Update category", description = "Partial update: only the fields present")
    @PutMapping("/api/v1/categories/{id}")
    public CategoryResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        return updateCategory.execute(id, request);
    }
}
