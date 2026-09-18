package com.api.sisventas.businessLogic.document;

import com.api.sisventas.dataSources.DocumentCounterRepository;
import com.api.sisventas.models.DocumentCounter;
import com.api.sisventas.models.DocumentKind;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;

/** Entrega el siguiente número del año para un tipo de documento: {@code F-2026-000001}. */
@Service
public class NextDocumentNumber {

    private static final String FORMAT = "%s-%d-%06d";

    private final DocumentCounterRepository documentCounterRepository;

    public NextDocumentNumber(DocumentCounterRepository documentCounterRepository) {
        this.documentCounterRepository = documentCounterRepository;
    }

    /**
     * Obligatoriamente dentro de la transacción del documento: el bloqueo del contador solo
     * sirve si se libera cuando el documento se confirma o se deshace.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String execute(DocumentKind kind, Instant documentDate) {
        int year = documentDate.atZone(ZoneOffset.UTC).getYear();
        String counterId = kind.prefix() + "-" + year;
        // Solo lectura con bloqueo: el contador lo siembra SeedDocumentCounters, nunca este camino.
        DocumentCounter counter = documentCounterRepository.findOneById(counterId)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el contador " + counterId + ": la siembra de arranque no corrió"));
        counter.setLastNumber(counter.getLastNumber() + 1);
        documentCounterRepository.save(counter);
        return String.format(FORMAT, kind.prefix(), year, counter.getLastNumber());
    }
}
