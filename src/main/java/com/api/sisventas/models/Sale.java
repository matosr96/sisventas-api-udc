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
 * Venta registrada. Sus líneas y su total son inmutables una vez creada: corregir una
 * venta es eliminarla (lo que devuelve el stock) y volver a registrarla.
 */
@Data
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Número presentable de la factura. Se deriva del id al registrar la venta, así que
     * es correlativo global y no por año ni por serie: si el negocio llega a exigirlo,
     * hace falta un contador propio, no un cambio de formato.
     */
    @Column(name = "sale_number", nullable = false, unique = true, length = 20)
    private String saleNumber;

    /**
     * Fecha del negocio: cuándo ocurrió la venta. Puede ser retroactiva, por eso no se
     * confunde con {@code createdAt}, que es cuándo se registró en el sistema.
     */
    @Column(name = "sale_date", nullable = false)
    private Instant saleDate;

    /** Suma de los subtotales de las líneas. Lo calcula el servidor, nunca el cliente. */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void addItem(SaleItem item) {
        item.setSale(this);
        items.add(item);
    }
}
