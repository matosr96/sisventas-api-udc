package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.dataSources.SaleReturnRepository;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anular una venta devuelve al catálogo las unidades que había descontado, con su asiento.
 * Una venta con devoluciones parciales ya no se anula: parte de sus unidades volvió por otra vía.
 */
@Service
public class DeleteSale {

    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final RecordStockMovement recordStockMovement;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public DeleteSale(SaleRepository saleRepository,
                      SaleReturnRepository saleReturnRepository,
                      RecordStockMovement recordStockMovement,
                      GetAuthenticatedUser getAuthenticatedUser) {
        this.saleRepository = saleRepository;
        this.saleReturnRepository = saleReturnRepository;
        this.recordStockMovement = recordStockMovement;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public void execute(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
        if (saleReturnRepository.existsBySaleId(id)) {
            throw new DomainError(ErrorCodes.SALE_HAS_RETURNS);
        }
        User actor = getAuthenticatedUser.execute();
        for (SaleItem item : sale.getItems()) {
            recordStockMovement.execute(item.getProduct().getId(), StockMovementType.SALE_VOID,
                    item.getQuantity(), sale.getSaleNumber(), null, actor);
        }
        saleRepository.delete(sale);
    }
}
