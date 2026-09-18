package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.UpdateUser;
import com.api.sisventas.models.dtos.user.UpdateUserRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class UpdateUserRoute {

    private final UpdateUser updateUser;

    public UpdateUserRoute(UpdateUser updateUser) {
        this.updateUser = updateUser;
    }

    @Operation(summary = "Update user", description = "Personal data of any user; ADMIN only")
    @PutMapping("/api/v1/users/{id}")
    public UserResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return updateUser.execute(id, request);
    }
}
