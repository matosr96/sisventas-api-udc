package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.document.NextDocumentNumber;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.dataSources.SaleReturnRepository;
import com.api.sisventas.models.DocumentKind;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
import com.api.sisventas.models.SaleReturn;
import com.api.sisventas.models.SaleReturnItem;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.sale.CreateSaleReturnRequest;
import com.api.sisventas.models.dtos.sale.SaleReturnItemRequest;
import com.api.sisventas.models.dtos.sale.SaleReturnResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Devolución parcial: cada línea devuelta reingresa al stock con un asiento SALE_RETURN y
 * suma al contador de devueltas de su línea vendida, que es el tope. El importe devuelto
 * se calcula al precio congelado de la línea; el descuento de la venta no se prorratea.
 */
@Service
public class CreateSaleReturn {

    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final NextDocumentNumber nextDocumentNumber;
    private final RecordStockMovement recordStockMovement;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public CreateSaleReturn(SaleRepository saleRepository,
                            SaleReturnRepository saleReturnRepository,
                            NextDocumentNumber nextDocumentNumber,
                            RecordStockMovement recordStockMovement,
                            GetAuthenticatedUser getAuthenticatedUser) {
        this.saleRepository = saleRepository;
        this.saleReturnRepository = saleReturnRepository;
        this.nextDocumentNumber = nextDocumentNumber;
        this.recordStockMovement = recordStockMovement;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public SaleReturnResponse execute(Long saleId, CreateSaleReturnRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
        User actor = getAuthenticatedUser.execute();
        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setSale(sale);
        saleReturn.setReason(request.reason().trim());
        saleReturn.setUser(actor);
        saleReturn.setReturnNumber(nextDocumentNumber.execute(DocumentKind.SALE_RETURN, Instant.now()));

        BigDecimal total = BigDecimal.ZERO;
        for (SaleReturnItemRequest line : request.items()) {
            SaleReturnItem item = buildItem(sale, line, saleReturn.getReturnNumber(), actor);
            saleReturn.addItem(item);
            total = total.add(item.getSubtotal());
        }
        saleReturn.setTotal(total);
        saleRepository.save(sale);
        return SaleReturnResponse.from(saleReturnRepository.save(saleReturn));
    }

    private SaleReturnItem buildItem(Sale sale, SaleReturnItemRequest line, String returnNumber, User actor) {
        SaleItem sold = sale.getItems().stream()
                .filter(item -> item.getId().equals(line.saleItemId()))
                .findFirst()
                .orElseThrow(() -> new DomainError(ErrorCodes.INVALID_REQUEST));
        int remaining = sold.getQuantity() - sold.getReturnedQuantity();
        if (line.quantity() > remaining) {
            throw new DomainError(ErrorCodes.RETURN_EXCEEDS_SOLD);
        }
        sold.setReturnedQuantity(sold.getReturnedQuantity() + line.quantity());
        recordStockMovement.execute(sold.getProduct().getId(), StockMovementType.SALE_RETURN,
                line.quantity(), returnNumber, null, actor);

        SaleReturnItem item = new SaleReturnItem();
        item.setSaleItem(sold);
        item.setProduct(sold.getProduct());
        item.setQuantity(line.quantity());
        item.setUnitPrice(sold.getUnitPrice());
        item.setSubtotal(sold.getUnitPrice().multiply(BigDecimal.valueOf(line.quantity())));
        return item;
    }
}
