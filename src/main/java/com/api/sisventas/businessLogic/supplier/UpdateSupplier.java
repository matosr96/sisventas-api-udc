package com.api.sisventas.businessLogic.supplier;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.Supplier;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import com.api.sisventas.models.dtos.supplier.UpdateSupplierRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateSupplier {

    private final SupplierRepository supplierRepository;

    public UpdateSupplier(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public SupplierResponse execute(Long id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SUPPLIER_NOT_FOUND));
        if (request.name() != null) {
            supplier.setName(request.name());
        }
        if (request.taxId() != null) {
            supplier.setTaxId(request.taxId());
        }
        if (request.phone() != null) {
            supplier.setPhone(request.phone());
        }
        if (request.email() != null) {
            supplier.setEmail(request.email());
        }
        if (request.status() != null) {
            supplier.setStatus(request.status());
        }
        try {
            return SupplierResponse.from(supplierRepository.saveAndFlush(supplier));
        } catch (DataIntegrityViolationException error) {
            if (SqlErrors.isUniqueViolation(error)) {
                throw new DomainError(ErrorCodes.SUPPLIER_NAME_ALREADY_EXISTS);
            }
            throw error;
        }
    }
}
