package com.api.sisventas.server;

import com.api.sisventas.businessLogic.document.SeedDocumentCounters;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Siembra los contadores de documentos al arrancar y cada día a las 00:05 UTC, para que el
 * cambio de año nunca pille a la API sin contador aunque el proceso lleve meses corriendo.
 */
@Component
public class DocumentCounterSeeder implements ApplicationRunner {

    private final SeedDocumentCounters seedDocumentCounters;

    public DocumentCounterSeeder(SeedDocumentCounters seedDocumentCounters) {
        this.seedDocumentCounters = seedDocumentCounters;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDocumentCounters.execute();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "UTC")
    public void daily() {
        seedDocumentCounters.execute();
    }
}
