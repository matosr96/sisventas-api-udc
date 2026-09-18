package com.api.sisventas.businessLogic.audit;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.AuditRepository;
import com.api.sisventas.models.dtos.audit.AuditResponse;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Escrituras registradas por AuditFilter: por usuario, método, recurso y rango de fechas. Solo ADMIN. */
@Service
public class ListAudits {

    private static final Map<String, String> SORTS = Map.of("createdAt", "createdAt", "username", "username");
    private static final String DEFAULT_SORT = "createdAt";

    private final AuditRepository repository;

    public ListAudits(AuditRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<AuditResponse> execute(String username, String method, String resource,
                                                Instant from, Instant to,
                                                String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.equal("username", blankToNull(username)),
                                Filters.equal("method", blankToNull(method)),
                                Filters.contains(resource, "resource"),
                                Filters.between("createdAt", from, to)),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                AuditResponse::from);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
