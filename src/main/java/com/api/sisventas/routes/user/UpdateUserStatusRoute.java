package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.UpdateUserStatus;
import com.api.sisventas.models.dtos.user.UserResponse;
import com.api.sisventas.models.dtos.user.UpdateUserStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class UpdateUserStatusRoute {

    private final UpdateUserStatus updateUserStatus;

    public UpdateUserStatusRoute(UpdateUserStatus updateUserStatus) {
        this.updateUserStatus = updateUserStatus;
    }

    @Operation(summary = "Activate or deactivate user",
            description = "An INACTIVE account cannot sign in nor use a live token; ADMIN only")
    @PutMapping("/api/v1/users/{id}/status")
    public UserResponse handle(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return updateUserStatus.execute(id, request);
    }
}
