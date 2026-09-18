package com.api.sisventas.routes.category;

import com.api.sisventas.businessLogic.category.GetCategory;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Categories")
public class GetCategoryRoute {

    private final GetCategory getCategory;

    public GetCategoryRoute(GetCategory getCategory) {
        this.getCategory = getCategory;
    }

    @Operation(summary = "Obtener categoría", description = "Devuelve una categoría por su id")
    @GetMapping("/api/v1/categories/{id}")
    public CategoryResponse handle(@PathVariable Long id) {
        return getCategory.execute(id);
    }
}
