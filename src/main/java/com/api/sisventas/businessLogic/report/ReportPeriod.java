package com.api.sisventas.businessLogic.report;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Rango [from, to) de un reporte en la zona horaria del negocio. Los días se cortan a
 * medianoche local, no UTC: un cierre de caja a las 23:30 en Bogotá es del mismo día.
 */
public record ReportPeriod(Instant from, Instant to) {

    public static ReportPeriod day(LocalDate day, ZoneId zone) {
        return new ReportPeriod(day.atStartOfDay(zone).toInstant(), day.plusDays(1).atStartOfDay(zone).toInstant());
    }

    /** Sin fechas: los últimos 30 días; con una sola, hasta hoy o desde hace 30 días. */
    public static ReportPeriod between(LocalDate from, LocalDate to, ZoneId zone, int defaultDays) {
        LocalDate end = to == null ? LocalDate.now(zone) : to;
        LocalDate start = from == null ? end.minusDays(defaultDays - 1L) : from;
        return new ReportPeriod(start.atStartOfDay(zone).toInstant(), end.plusDays(1).atStartOfDay(zone).toInstant());
    }
}
