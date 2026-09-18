package com.api.sisventas.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Compra a proveedor: la entrada de mercancía. Espejo de {@link Sale}: líneas con costo
 * unitario congelado, total calculado por el servidor e inmutable salvo la fecha. Anularla
 * retira del stock las unidades que había sumado.
 */
@Data
@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Correlativo del año, formato P-2026-000001. Lo entrega document_counters. */
    @Column(name = "purchase_number", nullable = false, unique = true, length = 20)
    private String purchaseNumber;

    /** Fecha del negocio (puede ser retroactiva); createdAt es cuándo se registró. */
    @Column(name = "purchase_date", nullable = false)
    private Instant purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /** Suma de los subtotales de las líneas. Lo calcula el servidor, nunca el cliente. */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void addItem(PurchaseItem item) {
        item.setPurchase(this);
        items.add(item);
    }
}
