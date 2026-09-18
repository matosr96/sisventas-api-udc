package com.api.sisventas.models.dtos.audit;

import com.api.sisventas.models.Audit;

import java.time.Instant;

public record AuditResponse(
        Long id, String username, String method, String resource, String detail, Instant createdAt) {

    public static AuditResponse from(Audit audit) {
        return new AuditResponse(audit.getId(), audit.getUsername(), audit.getMethod(), audit.getResource(),
                audit.getDetail(), audit.getCreatedAt());
    }
}
