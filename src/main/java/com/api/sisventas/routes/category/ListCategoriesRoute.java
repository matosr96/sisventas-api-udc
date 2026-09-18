package com.api.sisventas.routes.category;

import com.api.sisventas.businessLogic.category.ListCategories;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Categories")
public class ListCategoriesRoute {

    private final ListCategories listCategories;

    public ListCategoriesRoute(ListCategories listCategories) {
        this.listCategories = listCategories;
    }

    @Operation(summary = "List categories", description = "Filters: search (name); sort: name|createdAt")
    @GetMapping("/api/v1/categories")
    public PaginatedResponse<CategoryResponse> handle(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listCategories.execute(search, sort, dir, page, limit);
    }
}
