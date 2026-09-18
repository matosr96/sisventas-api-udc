package com.api.sisventas.businessLogic.report;

import com.api.sisventas.dataSources.ReportRepository;
import com.api.sisventas.models.dtos.report.SalesReportResponse;
import com.api.sisventas.models.dtos.report.TopProduct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Informe de ventas de un periodo. El margen es una estimación: usa el último costo
 * conocido de cada producto, no el costo que tenía cuando se vendió.
 */
@Service
public class GetSalesReport {

    private static final int DEFAULT_DAYS = 30;
    private static final int TOP_PRODUCTS = 10;

    private final ReportRepository reportRepository;
    private final ZoneId zone;

    public GetSalesReport(ReportRepository reportRepository, @Value("${app.business.time-zone}") String zone) {
        this.reportRepository = reportRepository;
        this.zone = ZoneId.of(zone);
    }

    @Transactional(readOnly = true)
    public SalesReportResponse execute(LocalDate from, LocalDate to) {
        ReportPeriod period = ReportPeriod.between(from, to, zone, DEFAULT_DAYS);
        List<TopProduct> products = reportRepository.topProducts(period.from(), period.to());
        BigDecimal margin = products.stream().map(TopProduct::margin).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SalesReportResponse(
                period.from(),
                period.to(),
                reportRepository.countSales(period.from(), period.to()),
                reportRepository.sumSales(period.from(), period.to()),
                margin,
                reportRepository.salesByDay(period.from(), period.to()),
                reportRepository.salesByUser(period.from(), period.to()),
                products.stream().limit(TOP_PRODUCTS).toList());
    }
}
