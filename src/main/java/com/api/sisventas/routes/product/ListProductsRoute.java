package com.api.sisventas.routes.product;

import com.api.sisventas.businessLogic.product.ListProducts;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.dtos.product.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products")
public class ListProductsRoute {

    private final ListProducts listProducts;

    public ListProductsRoute(ListProducts listProducts) {
        this.listProducts = listProducts;
    }

    @Operation(summary = "List products",
            description = "Paginated { count, page, pages, items }; filters: search (sku/name),"
                    + " status, categoryId, lowStock; sort: name|sku|price|stock|createdAt")
    @GetMapping("/api/v1/products")
    public PaginatedResponse<ProductResponse> handle(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listProducts.execute(search, status, categoryId, lowStock, sort, dir, page, limit);
    }
}
