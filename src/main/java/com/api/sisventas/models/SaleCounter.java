package com.api.sisventas.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contador del correlativo de facturas, una fila por año.
 *
 * Existe porque el número de factura no puede derivarse del id: con claves IDENTITY el
 * id solo se conoce después del INSERT, y {@code sales.sale_number} es NOT NULL. Además
 * un correlativo de verdad reinicia cada año, cosa que el id no hace.
 *
 * Se lee con bloqueo pesimista para que dos ventas simultáneas no tomen el mismo número.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_counters")
public class SaleCounter {

    @Id
    @Column(name = "year")
    private Integer year;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}
