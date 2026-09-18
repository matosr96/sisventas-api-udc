package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.UpdateOwnProfile;
import com.api.sisventas.models.dtos.user.UpdateUserRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class UpdateOwnProfileRoute {

    private final UpdateOwnProfile updateOwnProfile;

    public UpdateOwnProfileRoute(UpdateOwnProfile updateOwnProfile) {
        this.updateOwnProfile = updateOwnProfile;
    }

    @Operation(summary = "Update own profile", description = "Names and photo of the authenticated user")
    @PutMapping("/api/v1/users/me")
    public UserResponse handle(@Valid @RequestBody UpdateUserRequest request) {
        return updateOwnProfile.execute(request);
    }
}
