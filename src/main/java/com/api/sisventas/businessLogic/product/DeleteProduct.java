package com.api.sisventas.businessLogic.product;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.SaleItemRepository;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Un producto que alguna vez se vendió no se borra: se desactiva. Borrarlo destruiría
 * las líneas de venta que lo referencian, es decir el histórico de facturas. El borrado
 * real queda para lo que nunca llegó a usarse.
 */
@Service
public class DeleteProduct {

    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;

    public DeleteProduct(ProductRepository productRepository, SaleItemRepository saleItemRepository) {
        this.productRepository = productRepository;
        this.saleItemRepository = saleItemRepository;
    }

    @Transactional
    public void execute(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.PRODUCT_NOT_FOUND));
        if (saleItemRepository.existsByProductId(id)) {
            product.setStatus(ProductStatus.INACTIVE);
            productRepository.save(product);
            return;
        }
        productRepository.delete(product);
    }
}
