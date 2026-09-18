package com.api.sisventas.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Asiento inmutable del libro de stock. Toda variación de {@code products.current_stock}
 * deja exactamente un asiento; el stock de un producto es la suma de sus asientos, y
 * {@code stockAfter} guarda el saldo tras cada uno para poder auditar sin recalcular.
 */
@Data
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "type", nullable = false, length = 20)
    private StockMovementType type;

    /** Con signo: positivo entra, negativo sale. */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    /** Documento que lo originó (F-2026-000001, P-2026-000001), si lo hay. */
    @Column(name = "reference", length = 30)
    private String reference;

    /** Obligatorio en los ajustes: por qué se corrigió el stock. */
    @Column(name = "reason", length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
