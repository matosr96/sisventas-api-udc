package com.api.sisventas.routes.audit;

import com.api.sisventas.businessLogic.audit.ListAudits;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.dtos.audit.AuditResponse;

import java.time.Instant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Audits")
public class ListAuditsRoute {

    private final ListAudits listAudits;

    public ListAuditsRoute(ListAudits listAudits) {
        this.listAudits = listAudits;
    }

    @Operation(summary = "List audit entries",
            description = "ADMIN only. Filters: username, method, resource (contains), from, to"
                    + " ")
    @GetMapping("/api/v1/audits")
    public PaginatedResponse<AuditResponse> handle(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listAudits.execute(username, method, resource, from, to, sort, dir, page, limit);
    }
}
