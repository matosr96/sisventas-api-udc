package com.api.sisventas.routes.auth;

import com.api.sisventas.businessLogic.auth.SigninUser;
import com.api.sisventas.models.dtos.auth.AuthResponse;
import com.api.sisventas.models.dtos.auth.SigninRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth")
public class SigninRoute {

    private final SigninUser signinUser;

    public SigninRoute(SigninUser signinUser) {
        this.signinUser = signinUser;
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica y devuelve el token de acceso")
    @PostMapping("/api/v1/auth/signin")
    public AuthResponse handle(@Valid @RequestBody SigninRequest request) {
        return signinUser.execute(request);
    }
}
