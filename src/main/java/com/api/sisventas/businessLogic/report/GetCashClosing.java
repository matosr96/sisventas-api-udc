package com.api.sisventas.businessLogic.report;

import com.api.sisventas.dataSources.ReportRepository;
import com.api.sisventas.models.dtos.report.CashClosingResponse;
import com.api.sisventas.models.dtos.report.PaymentBreakdown;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Cierre de caja: lo cobrado en un día por método de pago, menos lo devuelto. Por vendedor si se pide. */
@Service
public class GetCashClosing {

    private final ReportRepository reportRepository;
    private final ZoneId zone;

    public GetCashClosing(ReportRepository reportRepository, @Value("${app.business.time-zone}") String zone) {
        this.reportRepository = reportRepository;
        this.zone = ZoneId.of(zone);
    }

    @Transactional(readOnly = true)
    public CashClosingResponse execute(LocalDate date, Long userId) {
        ReportPeriod period = ReportPeriod.day(date == null ? LocalDate.now(zone) : date, zone);
        List<PaymentBreakdown> breakdown = reportRepository.paymentBreakdown(period.from(), period.to(), userId);
        long count = breakdown.stream().mapToLong(PaymentBreakdown::count).sum();
        BigDecimal total = breakdown.stream().map(PaymentBreakdown::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal returned = reportRepository.sumReturns(period.from(), period.to(), userId);
        return new CashClosingResponse(period.from(), period.to(), userId, count, total, returned,
                total.subtract(returned), breakdown);
    }
}
