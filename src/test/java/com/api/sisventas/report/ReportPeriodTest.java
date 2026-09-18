package com.api.sisventas.businessLogic.report;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Los días se cortan en la zona del negocio, no en UTC. Sin Spring. */
class ReportPeriodTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Test
    void aDayRunsFromLocalMidnightToTheNextExclusive() {
        ReportPeriod day = ReportPeriod.day(LocalDate.of(2026, 9, 18), BOGOTA);
        assertEquals(ZonedDateTime.of(2026, 9, 18, 0, 0, 0, 0, BOGOTA).toInstant(), day.from());
        assertEquals(Duration.ofDays(1), Duration.between(day.from(), day.to()));
        assertEquals("2026-09-18T05:00:00Z", day.from().toString(), "medianoche de Bogotá son las 05:00 UTC");
    }

    @Test
    void betweenDefaultsToTheLastNDaysEndingToday() {
        LocalDate today = LocalDate.now(BOGOTA);
        ReportPeriod period = ReportPeriod.between(null, null, BOGOTA, 30);
        assertEquals(today.minusDays(29).atStartOfDay(BOGOTA).toInstant(), period.from());
        assertEquals(today.plusDays(1).atStartOfDay(BOGOTA).toInstant(), period.to());

        ReportPeriod explicit = ReportPeriod.between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), BOGOTA, 30);
        assertEquals(Duration.ofDays(31), Duration.between(explicit.from(), explicit.to()));
    }
}
