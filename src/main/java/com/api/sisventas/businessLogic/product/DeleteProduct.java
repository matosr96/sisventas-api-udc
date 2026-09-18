package com.api.sisventas.businessLogic.product;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.StockMovementRepository;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Un producto con histórico no se borra: se desactiva. Cualquier venta, compra o ajuste
 * —y el alta con stock— deja un asiento en el libro que lo referencia, así que la
 * pregunta correcta es "¿tiene asientos?", no "¿tiene ventas?". El borrado real queda
 * para lo que nunca llegó a moverse.
 */
@Service
public class DeleteProduct {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public DeleteProduct(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public void execute(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.PRODUCT_NOT_FOUND));
        if (stockMovementRepository.existsByProductId(id)) {
            product.setStatus(ProductStatus.INACTIVE);
            productRepository.save(product);
            return;
        }
        productRepository.delete(product);
    }
}
