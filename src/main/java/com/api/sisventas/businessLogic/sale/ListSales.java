package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListSales {

    private final SaleRepository saleRepository;

    public ListSales(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<SaleResponse> execute(int page, int limit) {
        return PaginatedResponse.from(
                saleRepository.findAll(Pagination.of(page, limit, "saleDate")),
                SaleResponse::from);
    }
}
