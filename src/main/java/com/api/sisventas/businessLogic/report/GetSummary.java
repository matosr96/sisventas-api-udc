package com.api.sisventas.businessLogic.report;

import com.api.sisventas.dataSources.ReportRepository;
import com.api.sisventas.models.dtos.report.SummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/** Cifras de Inicio calculadas en la base: no dependen de cuántas ventas quepan en una página. */
@Service
public class GetSummary {

    private final ReportRepository reportRepository;
    private final ZoneId zone;

    public GetSummary(ReportRepository reportRepository, @Value("${app.business.time-zone}") String zone) {
        this.reportRepository = reportRepository;
        this.zone = ZoneId.of(zone);
    }

    @Transactional(readOnly = true)
    public SummaryResponse execute() {
        LocalDate today = LocalDate.now(zone);
        ReportPeriod todayPeriod = ReportPeriod.day(today, zone);
        ReportPeriod yesterday = ReportPeriod.day(today.minusDays(1), zone);
        ReportPeriod month = new ReportPeriod(
                today.withDayOfMonth(1).atStartOfDay(zone).toInstant(),
                today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant());
        return new SummaryResponse(
                reportRepository.countSales(todayPeriod.from(), todayPeriod.to()),
                reportRepository.sumSales(todayPeriod.from(), todayPeriod.to()),
                reportRepository.countSales(yesterday.from(), yesterday.to()),
                reportRepository.sumSales(yesterday.from(), yesterday.to()),
                reportRepository.countPurchases(month.from(), month.to()),
                reportRepository.sumPurchases(month.from(), month.to()),
                reportRepository.countActiveProducts(),
                reportRepository.countLowStock(),
                reportRepository.inventoryValue());
    }
}
