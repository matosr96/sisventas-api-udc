package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
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
    private final NextSaleNumber nextSaleNumber;

    public CreateSale(SaleRepository saleRepository,
                      ProductRepository productRepository,
                      GetAuthenticatedUser getAuthenticatedUser,
                      NextSaleNumber nextSaleNumber) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
        this.nextSaleNumber = nextSaleNumber;
    }

    @Transactional
    public SaleResponse execute(CreateSaleRequest request) {
        Sale sale = new Sale();
        sale.setSaleDate(request.saleDate() == null ? Instant.now() : request.saleDate());
        sale.setSaleNumber(nextSaleNumber.execute(sale.getSaleDate()));
        sale.setUser(getAuthenticatedUser.execute());

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemRequest line : request.items()) {
            SaleItem item = buildItem(line);
            sale.addItem(item);
            total = total.add(item.getSubtotal());
        }
        sale.setTotal(total);

        return SaleResponse.from(saleRepository.save(sale));
    }

    private SaleItem buildItem(SaleItemRequest line) {
        Product product = productRepository.findById(line.productId())
                .orElseThrow(() -> new DomainError(ErrorCodes.PRODUCT_NOT_FOUND));
        takeFromStock(product, line.quantity());

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(line.quantity());
        item.setUnitPrice(product.getSalePrice());
        item.setSubtotal(product.getSalePrice().multiply(BigDecimal.valueOf(line.quantity())));
        return item;
    }

    /**
     * El producto es la misma instancia gestionada durante toda la transacción, así que
     * dos líneas del mismo producto descuentan de forma acumulada.
     */
    private void takeFromStock(Product product, int quantity) {
        if (product.getCurrentStock() < quantity) {
            throw new DomainError(ErrorCodes.INSUFFICIENT_STOCK);
        }
        product.setCurrentStock(product.getCurrentStock() - quantity);
    }
}
