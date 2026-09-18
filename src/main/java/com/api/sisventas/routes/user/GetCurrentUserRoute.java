package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.GetCurrentUser;
import com.api.sisventas.models.dtos.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class GetCurrentUserRoute {

    private final GetCurrentUser getCurrentUser;

    public GetCurrentUserRoute(GetCurrentUser getCurrentUser) {
        this.getCurrentUser = getCurrentUser;
    }

    @Operation(summary = "My profile", description = "The user behind the token")
    @GetMapping("/api/v1/users/me")
    public UserResponse handle() {
        return getCurrentUser.execute();
    }
}
