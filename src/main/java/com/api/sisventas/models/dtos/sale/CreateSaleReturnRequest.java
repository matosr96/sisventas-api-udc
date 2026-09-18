package com.api.sisventas.models.dtos.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSaleReturnRequest(
        @NotBlank @Size(max = 255) String reason,
        @NotEmpty @Valid List<SaleReturnItemRequest> items) {
}
