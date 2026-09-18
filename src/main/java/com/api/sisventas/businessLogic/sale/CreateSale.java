package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.document.NextDocumentNumber;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.DocumentKind;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registra una venta: congela el precio de cada línea, descuenta el stock y calcula el
 * total. Las tres cosas ocurren en la misma transacción, así que una línea sin stock
 * deshace la venta entera.
 */
@Service
public class CreateSale {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;
    private final NextDocumentNumber nextDocumentNumber;
    private final RecordStockMovement recordStockMovement;

    public CreateSale(SaleRepository saleRepository,
                      ProductRepository productRepository,
                      GetAuthenticatedUser getAuthenticatedUser,
                      NextDocumentNumber nextDocumentNumber,
                      RecordStockMovement recordStockMovement) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
        this.nextDocumentNumber = nextDocumentNumber;
        this.recordStockMovement = recordStockMovement;
    }

    @Transactional
    public SaleResponse execute(CreateSaleRequest request) {
        User seller = getAuthenticatedUser.execute();
        Sale sale = new Sale();
        sale.setSaleDate(request.saleDate() == null ? Instant.now() : request.saleDate());
        sale.setSaleNumber(nextDocumentNumber.execute(DocumentKind.SALE, sale.getSaleDate()));
        sale.setUser(seller);

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemRequest line : request.items()) {
            SaleItem item = buildItem(line, sale.getSaleNumber(), seller);
            sale.addItem(item);
            total = total.add(item.getSubtotal());
        }
        sale.setTotal(total);

        return SaleResponse.from(saleRepository.save(sale));
    }

    private SaleItem buildItem(SaleItemRequest line, String saleNumber, User seller) {
        Product product = productRepository.findById(line.productId())
                .orElseThrow(() -> new DomainError(ErrorCodes.PRODUCT_NOT_FOUND));
        recordStockMovement.execute(product, StockMovementType.SALE, -line.quantity(), saleNumber, null, seller);

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(line.quantity());
        item.setUnitPrice(product.getSalePrice());
        item.setSubtotal(product.getSalePrice().multiply(BigDecimal.valueOf(line.quantity())));
        return item;
    }

}
