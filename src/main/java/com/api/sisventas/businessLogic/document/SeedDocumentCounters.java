package com.api.sisventas.businessLogic.document;

import com.api.sisventas.dataSources.DocumentCounterRepository;
import com.api.sisventas.models.DocumentCounter;
import com.api.sisventas.models.DocumentKind;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.time.ZoneOffset;

/**
 * Deja creados los contadores del año en curso y el siguiente para todos los tipos de
 * documento. Corre al arrancar y una vez al día, sin contención.
 *
 * Existe para que emitir un número nunca tenga que crear el contador: crearlo en la misma
 * transacción del documento provoca deadlocks en MySQL (INSERT IGNORE + FOR UPDATE), y en
 * una transacción anidada agota el pool de conexiones bajo carga. Con la fila siempre
 * presente, el camino caliente es una única lectura con bloqueo, que se serializa sin más.
 */
@Service
public class SeedDocumentCounters {

    private static final int YEARS_AHEAD = 1;

    private final DocumentCounterRepository documentCounterRepository;

    public SeedDocumentCounters(DocumentCounterRepository documentCounterRepository) {
        this.documentCounterRepository = documentCounterRepository;
    }

    @Transactional
    public void execute() {
        int currentYear = Year.now(ZoneOffset.UTC).getValue();
        for (int year = currentYear; year <= currentYear + YEARS_AHEAD; year++) {
            for (DocumentKind kind : DocumentKind.values()) {
                String id = kind.prefix() + "-" + year;
                if (!documentCounterRepository.existsById(id)) {
                    documentCounterRepository.save(new DocumentCounter(id, 0L));
                }
            }
        }
    }
}
