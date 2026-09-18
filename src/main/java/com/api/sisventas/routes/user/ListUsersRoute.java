package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.ListUsers;
import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.models.dtos.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class ListUsersRoute {

    private final ListUsers listUsers;

    public ListUsersRoute(ListUsers listUsers) {
        this.listUsers = listUsers;
    }

    @Operation(summary = "List users", description = "Paginated list; ADMIN only")
    @GetMapping("/api/v1/users")
    public PaginatedResponse<UserResponse> handle(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LIMIT) int limit) {
        return listUsers.execute(page, limit);
    }
}
