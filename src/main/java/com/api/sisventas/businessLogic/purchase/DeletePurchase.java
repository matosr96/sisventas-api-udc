package com.api.sisventas.businessLogic.purchase;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.models.Purchase;
import com.api.sisventas.models.PurchaseItem;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anular una compra retira del stock lo que había sumado. Si esas unidades ya se vendieron,
 * el saldo quedaría negativo y el libro lo rechaza con 621: no se puede deshacer una entrada
 * cuya mercancía ya salió.
 */
@Service
public class DeletePurchase {

    private final PurchaseRepository purchaseRepository;
    private final RecordStockMovement recordStockMovement;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public DeletePurchase(PurchaseRepository purchaseRepository,
                          RecordStockMovement recordStockMovement,
                          GetAuthenticatedUser getAuthenticatedUser) {
        this.purchaseRepository = purchaseRepository;
        this.recordStockMovement = recordStockMovement;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public void execute(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.PURCHASE_NOT_FOUND));
        User actor = getAuthenticatedUser.execute();
        for (PurchaseItem item : purchase.getItems()) {
            recordStockMovement.execute(item.getProduct().getId(), StockMovementType.PURCHASE_VOID,
                    -item.getQuantity(), purchase.getPurchaseNumber(), null, actor);
        }
        purchaseRepository.delete(purchase);
    }
}
