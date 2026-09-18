package com.api.sisventas.businessLogic.product;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.models.dtos.product.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListProducts {

    private final ProductRepository productRepository;

    public ListProducts(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> execute(int page, int limit) {
        return PaginatedResponse.from(
                productRepository.findAll(Pagination.of(page, limit, "createdAt")),
                ProductResponse::from);
    }
}
