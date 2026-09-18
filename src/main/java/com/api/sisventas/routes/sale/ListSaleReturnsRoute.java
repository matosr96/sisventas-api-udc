package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.ListSaleReturns;
import com.api.sisventas.models.dtos.sale.SaleReturnResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Sales")
public class ListSaleReturnsRoute {

    private final ListSaleReturns listSaleReturns;

    public ListSaleReturnsRoute(ListSaleReturns listSaleReturns) {
        this.listSaleReturns = listSaleReturns;
    }

    @Operation(summary = "List returns of a sale", description = "Most recent first")
    @GetMapping("/api/v1/sales/{id}/returns")
    public List<SaleReturnResponse> handle(@PathVariable Long id) {
        return listSaleReturns.execute(id);
    }
}
