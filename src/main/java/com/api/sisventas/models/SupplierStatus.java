package com.api.sisventas.models;

/** Estado de un proveedor. Reflejado con un CHECK en la tabla. */
public enum SupplierStatus {
    ACTIVE,
    /** Retirado. Se conserva porque tiene compras asociadas. */
    INACTIVE
}
