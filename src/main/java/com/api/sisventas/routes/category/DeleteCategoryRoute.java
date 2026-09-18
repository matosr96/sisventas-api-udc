package com.api.sisventas.routes.category;

import com.api.sisventas.businessLogic.category.DeleteCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Categories")
public class DeleteCategoryRoute {

    private final DeleteCategory deleteCategory;

    public DeleteCategoryRoute(DeleteCategory deleteCategory) {
        this.deleteCategory = deleteCategory;
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría por su id")
    @DeleteMapping("/api/v1/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable Long id) {
        deleteCategory.execute(id);
    }
}
