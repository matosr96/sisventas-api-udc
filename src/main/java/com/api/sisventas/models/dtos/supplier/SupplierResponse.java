package com.api.sisventas.models.dtos.supplier;

import com.api.sisventas.models.Supplier;
import com.api.sisventas.models.SupplierStatus;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String name,
        String taxId,
        String phone,
        String email,
        SupplierStatus status,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getTaxId(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getStatus(),
                supplier.getCreatedBy() == null ? null : supplier.getCreatedBy().getId(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt());
    }
}
