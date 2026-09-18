package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.dataSources.SaleCounterRepository;
import com.api.sisventas.models.SaleCounter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;

/** Entrega el siguiente número de factura del año, con el formato {@code F-2026-000001}. */
@Service
public class NextSaleNumber {

    private static final String FORMAT = "F-%d-%06d";

    private final SaleCounterRepository saleCounterRepository;

    public NextSaleNumber(SaleCounterRepository saleCounterRepository) {
        this.saleCounterRepository = saleCounterRepository;
    }

    /**
     * Obligatoriamente dentro de la transacción de la venta: el bloqueo del contador solo
     * sirve si se libera cuando la venta se confirma o se deshace.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String execute(Instant saleDate) {
        int year = saleDate.atZone(ZoneOffset.UTC).getYear();
        SaleCounter counter = saleCounterRepository.findByYear(year)
                .orElseGet(() -> new SaleCounter(year, 0L));
        counter.setLastNumber(counter.getLastNumber() + 1);
        saleCounterRepository.save(counter);
        return String.format(FORMAT, year, counter.getLastNumber());
    }
}
