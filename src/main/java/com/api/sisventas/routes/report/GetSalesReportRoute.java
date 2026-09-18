package com.api.sisventas.routes.report;

import com.api.sisventas.businessLogic.report.GetSalesReport;
import com.api.sisventas.models.dtos.report.SalesReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Tag(name = "Reports")
public class GetSalesReportRoute {

    private final GetSalesReport getSalesReport;

    public GetSalesReportRoute(GetSalesReport getSalesReport) {
        this.getSalesReport = getSalesReport;
    }

    @Operation(summary = "Sales report",
            description = "Totals, daily series, by seller and top products with estimated margin;"
                    + " from/to as yyyy-MM-dd (default: last 30 days)")
    @GetMapping("/api/v1/reports/sales")
    public SalesReportResponse handle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return getSalesReport.execute(from, to);
    }
}
