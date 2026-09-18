package com.api.sisventas.businessLogic.supplier;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.Supplier;
import com.api.sisventas.models.SupplierStatus;
import com.api.sisventas.models.dtos.supplier.CreateSupplierRequest;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSupplier {

    private final SupplierRepository supplierRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public CreateSupplier(SupplierRepository supplierRepository, GetAuthenticatedUser getAuthenticatedUser) {
        this.supplierRepository = supplierRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public SupplierResponse execute(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setTaxId(request.taxId());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setStatus(SupplierStatus.ACTIVE);
        supplier.setCreatedBy(getAuthenticatedUser.execute());
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
