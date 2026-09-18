package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSale {

    private final SaleRepository saleRepository;

    public GetSale(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public SaleResponse execute(Long id) {
        return saleRepository.findById(id)
                .map(SaleResponse::from)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
    }
}
