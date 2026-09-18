package com.api.sisventas.routes.report;

import com.api.sisventas.businessLogic.report.GetCashClosing;
import com.api.sisventas.models.dtos.report.CashClosingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Tag(name = "Reports")
public class GetCashClosingRoute {

    private final GetCashClosing getCashClosing;

    public GetCashClosingRoute(GetCashClosing getCashClosing) {
        this.getCashClosing = getCashClosing;
    }

    @Operation(summary = "Cash closing",
            description = "Takings of one day by payment method, minus returns; optionally per seller")
    @GetMapping("/api/v1/reports/closing")
    public CashClosingResponse handle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long userId) {
        return getCashClosing.execute(date, userId);
    }
}
