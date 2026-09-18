package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import com.api.sisventas.models.dtos.sale.UpdateSaleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solo corrige la fecha. Las líneas y el total de una venta registrada son inmutables:
 * cambiarlos reescribiría una factura y descuadraría el stock. Para rectificar de
 * verdad hay que eliminar la venta (que devuelve el stock) y registrarla de nuevo.
 */
@Service
public class UpdateSale {

    private final SaleRepository saleRepository;

    public UpdateSale(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional
    public SaleResponse execute(Long id, UpdateSaleRequest request) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
        if (request.saleDate() != null) {
            sale.setSaleDate(request.saleDate());
        }
        return SaleResponse.from(saleRepository.save(sale));
    }
}
