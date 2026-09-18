package com.api.sisventas.businessLogic.supplier;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.SupplierStatus;
import com.api.sisventas.models.dtos.supplier.SupplierResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Proveedores por texto (nombre, NIT, correo) y estado; orden por nombre o fecha de alta. */
@Service
public class ListSuppliers {

    private static final Map<String, String> SORTS = Map.of("name", "name", "createdAt", "createdAt");
    private static final String DEFAULT_SORT = "createdAt";

    private final SupplierRepository repository;

    public ListSuppliers(SupplierRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<SupplierResponse> execute(String search, SupplierStatus status,
                                                    String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.contains(search, "name", "taxId", "email"),
                                Filters.equal("status", status)),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                SupplierResponse::from);
    }
}
