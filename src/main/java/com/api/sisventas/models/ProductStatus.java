package com.api.sisventas.models;

/** Estado de un producto en el catálogo. Reflejado con un CHECK en la tabla. */
public enum ProductStatus {
    /** Se puede vender y aparece en el catálogo. */
    ACTIVE,
    /** Retirado del catálogo. Se conserva porque tiene ventas asociadas. */
    INACTIVE
}
