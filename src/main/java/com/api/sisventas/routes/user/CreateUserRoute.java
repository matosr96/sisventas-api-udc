package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.CreateUser;
import com.api.sisventas.models.dtos.user.CreateUserRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class CreateUserRoute {

    private final CreateUser createUser;

    public CreateUserRoute(CreateUser createUser) {
        this.createUser = createUser;
    }

    @Operation(summary = "Create user",
            description = "ADMIN creates an account with an initial role (default USER)")
    @PostMapping("/api/v1/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse handle(@Valid @RequestBody CreateUserRequest request) {
        return createUser.execute(request);
    }
}
