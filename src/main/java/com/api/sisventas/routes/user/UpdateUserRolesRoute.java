package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.UpdateUserRoles;
import com.api.sisventas.models.dtos.auth.UserResponse;
import com.api.sisventas.models.dtos.user.UpdateUserRolesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class UpdateUserRolesRoute {

    private final UpdateUserRoles updateUserRoles;

    public UpdateUserRolesRoute(UpdateUserRoles updateUserRoles) {
        this.updateUserRoles = updateUserRoles;
    }

    @Operation(summary = "Asignar roles", description = "Reemplaza los roles de un usuario; solo ADMIN")
    @PutMapping("/api/v1/users/{id}/roles")
    public UserResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return updateUserRoles.execute(id, request);
    }
}
