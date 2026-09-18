package com.api.sisventas.businessLogic.purchase;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPurchases {

    private final PurchaseRepository purchaseRepository;

    public ListPurchases(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<PurchaseResponse> execute(int page, int limit) {
        return PaginatedResponse.from(
                purchaseRepository.findAll(Pagination.of(page, limit, "purchaseDate")),
                PurchaseResponse::from);
    }
}
