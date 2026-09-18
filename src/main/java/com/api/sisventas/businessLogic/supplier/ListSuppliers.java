package com.api.sisventas.businessLogic.supplier;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListSuppliers {

    private final SupplierRepository supplierRepository;

    public ListSuppliers(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<SupplierResponse> execute(int page, int limit) {
        return PaginatedResponse.from(
                supplierRepository.findAll(Pagination.of(page, limit, "createdAt")),
                SupplierResponse::from);
    }
}
