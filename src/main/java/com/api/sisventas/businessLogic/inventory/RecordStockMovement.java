package com.api.sisventas.businessLogic.inventory;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.ProductRepository;
import com.api.sisventas.dataSources.StockMovementRepository;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.StockMovement;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único punto por el que cambia {@code products.current_stock}. Carga el producto con
 * bloqueo de fila, aplica la variación y deja el asiento, todo en la transacción de la
 * operación que lo origina: nadie más toca el stock.
 *
 * Recibe el id y no la entidad a propósito: el bloqueo tiene que ser la PRIMERA lectura
 * del producto en la transacción. Un producto ya cargado sin bloqueo conserva un saldo
 * viejo aunque después se bloquee la fila.
 */
@Service
public class RecordStockMovement {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public RecordStockMovement(ProductRepository productRepository,
                               StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * @param quantity con signo: positivo entra, negativo sale.
     * @return el asiento; su {@code getProduct()} es el producto bloqueado y al día.
     * @throws DomainError {@link ErrorCodes#PRODUCT_NOT_FOUND} o
     *         {@link ErrorCodes#INSUFFICIENT_STOCK} si el saldo quedaría negativo.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public StockMovement execute(Long productId, StockMovementType type, int quantity,
                                 String reference, String reason, User user) {
        Product product = productRepository.findForUpdateById(productId)
                .orElseThrow(() -> new DomainError(ErrorCodes.PRODUCT_NOT_FOUND));
        int after = product.getCurrentStock() + quantity;
        if (after < 0) {
            throw new DomainError(ErrorCodes.INSUFFICIENT_STOCK);
        }
        product.setCurrentStock(after);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setStockAfter(after);
        movement.setReference(reference);
        movement.setReason(reason);
        movement.setUser(user);
        return stockMovementRepository.save(movement);
    }
}
