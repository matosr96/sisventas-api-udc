package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Anular una venta devuelve al catálogo las unidades que había descontado. */
@Service
public class DeleteSale {

    private final SaleRepository saleRepository;

    public DeleteSale(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional
    public void execute(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setCurrentStock(product.getCurrentStock() + item.getQuantity());
        }
        saleRepository.delete(sale);
    }
}
