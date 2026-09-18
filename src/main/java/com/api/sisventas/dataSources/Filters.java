package com.api.sisventas.dataSources;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtros opcionales de los listados como {@link Specification}: cada parámetro nulo o
 * vacío no añade condición. Vive en la capa de datos porque habla en términos del ORM.
 */
public final class Filters {

    private Filters() {
    }

    /** {@code LIKE %texto%} sin distinguir mayúsculas sobre uno o más campos (OR). */
    public static <T> Specification<T> contains(String text, String... fields) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, builder) -> {
            List<Predicate> any = new ArrayList<>();
            for (String field : fields) {
                any.add(builder.like(builder.lower(root.get(field)), pattern));
            }
            return builder.or(any.toArray(new Predicate[0]));
        };
    }

    public static <T> Specification<T> equal(String field, Object value) {
        return value == null ? null : (root, query, builder) -> builder.equal(root.get(field), value);
    }

    /** Igualdad sobre el id de una relación: {@code root.join(relation).id = value}. */
    public static <T> Specification<T> relationId(String relation, Long value) {
        return value == null ? null : (root, query, builder) -> builder.equal(root.get(relation).get("id"), value);
    }

    public static <T> Specification<T> between(String field, Instant from, Instant to) {
        return (root, query, builder) -> {
            List<Predicate> all = new ArrayList<>();
            if (from != null) {
                all.add(builder.greaterThanOrEqualTo(root.get(field), from));
            }
            if (to != null) {
                all.add(builder.lessThanOrEqualTo(root.get(field), to));
            }
            return builder.and(all.toArray(new Predicate[0]));
        };
    }

    /** Stock bajo: {@code current_stock <= low_stock} con umbral definido. */
    public static <T> Specification<T> lowStock(Boolean only) {
        return Boolean.TRUE.equals(only)
                ? (root, query, builder) -> builder.and(
                        builder.isNotNull(root.get("lowStock")),
                        builder.lessThanOrEqualTo(root.get("currentStock"), root.get("lowStock")))
                : null;
    }

    /** Combina ignorando las nulas, para que cada filtro pueda "no aplicar". */
    @SafeVarargs
    public static <T> Specification<T> all(Specification<T>... specifications) {
        Specification<T> combined = Specification.where(null);
        for (Specification<T> specification : specifications) {
            if (specification != null) {
                combined = combined.and(specification);
            }
        }
        return combined;
    }
}
