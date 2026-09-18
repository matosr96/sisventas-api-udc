package com.api.sisventas.routes.auth;

import com.api.sisventas.businessLogic.auth.SignupUser;
import com.api.sisventas.models.dtos.auth.AuthResponse;
import com.api.sisventas.models.dtos.auth.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth")
public class SignupRoute {

    private final SignupUser signupUser;

    public SignupRoute(SignupUser signupUser) {
        this.signupUser = signupUser;
    }

    @Operation(summary = "Sign up", description = "Creates a user with the USER role and returns its token")
    @PostMapping("/api/v1/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse handle(@Valid @RequestBody SignupRequest request) {
        return signupUser.execute(request);
    }
}
