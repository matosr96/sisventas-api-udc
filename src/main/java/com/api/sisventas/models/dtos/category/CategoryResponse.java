package com.api.sisventas.models.dtos.category;

import com.api.sisventas.models.Category;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String icon,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.getCreatedBy() == null ? null : category.getCreatedBy().getId(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
