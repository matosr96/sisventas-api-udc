package com.api.sisventas.businessLogic.purchase;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.models.Purchase;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import com.api.sisventas.models.dtos.purchase.UpdatePurchaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Solo corrige la fecha: las líneas y el total de una compra registrada son inmutables. */
@Service
public class UpdatePurchase {

    private final PurchaseRepository purchaseRepository;

    public UpdatePurchase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public PurchaseResponse execute(Long id, UpdatePurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.PURCHASE_NOT_FOUND));
        if (request.purchaseDate() != null) {
            purchase.setPurchaseDate(request.purchaseDate());
        }
        return PurchaseResponse.from(purchaseRepository.save(purchase));
    }
}
