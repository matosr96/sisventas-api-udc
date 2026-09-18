package com.api.sisventas.businessLogic.supplier;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.Supplier;
import com.api.sisventas.models.SupplierStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Un proveedor con compras no se borra: se desactiva, para no romper el histórico. */
@Service
public class DeleteSupplier {

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;

    public DeleteSupplier(SupplierRepository supplierRepository, PurchaseRepository purchaseRepository) {
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public void execute(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SUPPLIER_NOT_FOUND));
        if (purchaseRepository.existsBySupplierId(id)) {
            supplier.setStatus(SupplierStatus.INACTIVE);
            supplierRepository.save(supplier);
            return;
        }
        supplierRepository.delete(supplier);
    }
}
