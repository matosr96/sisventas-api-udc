package com.api.sisventas.models.dtos.sale;

import java.time.Instant;

/**
 * Lo único corregible de una venta registrada es su fecha. Las líneas y el total son
 * inmutables: cambiarlos significaría reescribir una factura y descuadrar el stock.
 * Para rectificar una venta se elimina (devuelve el stock) y se registra de nuevo.
 */
public record UpdateSaleRequest(Instant saleDate) {
}
