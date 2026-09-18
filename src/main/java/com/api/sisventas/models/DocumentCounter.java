package com.api.sisventas.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Correlativo de documentos, una fila por tipo y año ({@code id = "F-2026"}).
 *
 * Existe porque el número no puede derivarse del id: con claves IDENTITY el id solo se
 * conoce después del INSERT, y el número es NOT NULL. Además un correlativo de verdad
 * reinicia cada año, cosa que el id no hace. Se lee con bloqueo pesimista para que dos
 * documentos simultáneos no tomen el mismo número.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_counters")
public class DocumentCounter {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}
