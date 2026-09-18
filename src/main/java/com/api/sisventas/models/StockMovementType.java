package com.api.sisventas.models;

/** Motivo de cada asiento del libro de stock. El signo va en la cantidad, no aquí. */
public enum StockMovementType {
    /** Stock con el que se dio de alta el producto. */
    INITIAL,
    /** Entrada por compra a proveedor. */
    PURCHASE,
    /** Reversión de una compra anulada. */
    PURCHASE_VOID,
    /** Salida por venta. */
    SALE,
    /** Reversión de una venta anulada. */
    SALE_VOID,
    /** Entrada por devolución parcial de una venta. */
    SALE_RETURN,
    /** Corrección manual con motivo: merma, conteo, rotura. */
    ADJUSTMENT
}
