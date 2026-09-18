package com.api.sisventas.routes.report;

import com.api.sisventas.businessLogic.report.GetSummary;
import com.api.sisventas.models.dtos.report.SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Reports")
public class GetSummaryRoute {

    private final GetSummary getSummary;

    public GetSummaryRoute(GetSummary getSummary) {
        this.getSummary = getSummary;
    }

    @Operation(summary = "Home summary",
            description = "Today vs. yesterday, this month's purchases and inventory status")
    @GetMapping("/api/v1/reports/summary")
    public SummaryResponse handle() {
        return getSummary.execute();
    }
}
