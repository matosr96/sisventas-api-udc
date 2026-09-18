package com.api.sisventas.businessLogic.user;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.Filters;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.UserStatus;
import com.api.sisventas.models.dtos.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Usuarios por texto (usuario, nombre, apellido) y estado; orden por usuario o fecha de alta. */
@Service
public class ListUsers {

    private static final Map<String, String> SORTS = Map.of(
            "username", "username", "name", "firstName", "createdAt", "createdAt");
    private static final String DEFAULT_SORT = "createdAt";

    private final UserRepository repository;

    public ListUsers(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponse> execute(String search, UserStatus status,
                                                String sort, String dir, int page, int limit) {
        return PaginatedResponse.from(
                repository.findAll(
                        Filters.all(
                                Filters.contains(search, "username", "firstName", "lastName"),
                                Filters.equal("status", status)),
                        Pagination.of(page, limit, sort, dir, SORTS, DEFAULT_SORT)),
                UserResponse::from);
    }
}
