package com.api.sisventas.businessLogic.purchase;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPurchase {

    private final PurchaseRepository purchaseRepository;

    public GetPurchase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional(readOnly = true)
    public PurchaseResponse execute(Long id) {
        return purchaseRepository.findById(id)
                .map(PurchaseResponse::from)
                .orElseThrow(() -> new DomainError(ErrorCodes.PURCHASE_NOT_FOUND));
    }
}
