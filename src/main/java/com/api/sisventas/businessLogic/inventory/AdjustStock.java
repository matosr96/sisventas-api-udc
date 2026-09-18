package com.api.sisventas.businessLogic.inventory;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.dtos.inventory.AdjustStockRequest;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ajuste manual con motivo (merma, conteo, rotura). Un ajuste de cero no dice nada y se rechaza. */
@Service
public class AdjustStock {

    private final RecordStockMovement recordStockMovement;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public AdjustStock(RecordStockMovement recordStockMovement,
                       GetAuthenticatedUser getAuthenticatedUser) {
        this.recordStockMovement = recordStockMovement;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public StockMovementResponse execute(Long productId, AdjustStockRequest request) {
        if (request.quantity() == 0) {
            throw new DomainError(ErrorCodes.INVALID_REQUEST);
        }
        return StockMovementResponse.from(recordStockMovement.execute(
                productId, StockMovementType.ADJUSTMENT, request.quantity(),
                null, request.reason(), getAuthenticatedUser.execute()));
    }
}
