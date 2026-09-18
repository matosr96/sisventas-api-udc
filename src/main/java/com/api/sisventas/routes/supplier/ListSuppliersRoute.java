package com.api.sisventas.routes.supplier;

import com.api.sisventas.businessLogic.supplier.ListSuppliers;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.SupplierStatus;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Suppliers")
public class ListSuppliersRoute {

    private final ListSuppliers listSuppliers;

    public ListSuppliersRoute(ListSuppliers listSuppliers) {
        this.listSuppliers = listSuppliers;
    }

    @Operation(summary = "List suppliers",
            description = "Filters: search (name/taxId/email), status; sort: name|createdAt"
                    + " ")
    @GetMapping("/api/v1/suppliers")
    public PaginatedResponse<SupplierResponse> handle(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listSuppliers.execute(search, status, sort, dir, page, limit);
    }
}
