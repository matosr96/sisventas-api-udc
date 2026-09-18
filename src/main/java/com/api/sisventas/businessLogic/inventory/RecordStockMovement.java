package com.api.sisventas.businessLogic.inventory;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.StockMovementRepository;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.StockMovement;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único punto por el que cambia {@code products.current_stock}. Aplica la variación al
 * producto y deja el asiento correspondiente en el libro, en la misma transacción que la
 * operación que lo origina: nadie más toca el stock.
 *
 * El producto llega como instancia gestionada, así que varias líneas del mismo producto
 * dentro de una operación acumulan correctamente.
 */
@Service
public class RecordStockMovement {

    private final StockMovementRepository stockMovementRepository;

    public RecordStockMovement(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * @param quantity con signo: positivo entra, negativo sale.
     * @throws DomainError {@link ErrorCodes#INSUFFICIENT_STOCK} si el saldo quedaría negativo.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public StockMovement execute(Product product, StockMovementType type, int quantity,
                                 String reference, String reason, User user) {
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
