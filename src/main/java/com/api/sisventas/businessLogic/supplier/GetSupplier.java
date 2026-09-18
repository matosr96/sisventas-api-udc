package com.api.sisventas.businessLogic.supplier;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSupplier {

    private final SupplierRepository supplierRepository;

    public GetSupplier(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public SupplierResponse execute(Long id) {
        return supplierRepository.findById(id)
                .map(SupplierResponse::from)
                .orElseThrow(() -> new DomainError(ErrorCodes.SUPPLIER_NOT_FOUND));
    }
}
