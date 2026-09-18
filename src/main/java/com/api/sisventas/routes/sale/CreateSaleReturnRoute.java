package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.CreateSaleReturn;
import com.api.sisventas.models.dtos.sale.CreateSaleReturnRequest;
import com.api.sisventas.models.dtos.sale.SaleReturnResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class CreateSaleReturnRoute {

    private final CreateSaleReturn createSaleReturn;

    public CreateSaleReturnRoute(CreateSaleReturn createSaleReturn) {
        this.createSaleReturn = createSaleReturn;
    }

    @Operation(summary = "Register partial return",
            description = "Returns units of one or more lines to stock; never more than what was sold")
    @PostMapping("/api/v1/sales/{id}/returns")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleReturnResponse handle(@PathVariable Long id,
                                     @Valid @RequestBody CreateSaleReturnRequest request) {
        return createSaleReturn.execute(id, request);
    }
}
