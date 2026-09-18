package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.dataSources.SaleReturnRepository;
import com.api.sisventas.models.dtos.sale.SaleReturnResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Devoluciones de una venta, de la más reciente a la más antigua. Sin paginar: son pocas por venta. */
@Service
public class ListSaleReturns {

    private final SaleRepository saleRepository;
    private final SaleReturnRepository saleReturnRepository;

    public ListSaleReturns(SaleRepository saleRepository, SaleReturnRepository saleReturnRepository) {
        this.saleRepository = saleRepository;
        this.saleReturnRepository = saleReturnRepository;
    }

    @Transactional(readOnly = true)
    public List<SaleReturnResponse> execute(Long saleId) {
        if (!saleRepository.existsById(saleId)) {
            throw new DomainError(ErrorCodes.SALE_NOT_FOUND);
        }
        return saleReturnRepository.findBySaleIdOrderByCreatedAtDesc(saleId).stream()
                .map(SaleReturnResponse::from)
                .toList();
    }
}
