package com.api.sisventas.models.dtos.inventory;

import com.api.sisventas.models.StockMovement;
import com.api.sisventas.models.StockMovementType;

import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        StockMovementType type,
        Integer quantity,
        Integer stockAfter,
        String reference,
        String reason,
        Long userId,
        Instant createdAt) {

    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getProduct().getName(),
                movement.getType(),
                movement.getQuantity(),
                movement.getStockAfter(),
                movement.getReference(),
                movement.getReason(),
                movement.getUser().getId(),
                movement.getCreatedAt());
    }
}
