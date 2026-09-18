package com.api.sisventas.models;

/** Documentos con correlativo propio. El prefijo es lo que ve el cliente: F-2026-000001. */
public enum DocumentKind {
    SALE("F"),
    PURCHASE("P");

    private final String prefix;

    DocumentKind(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
