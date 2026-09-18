package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.ChangeOwnPassword;
import com.api.sisventas.models.dtos.user.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class ChangeOwnPasswordRoute {

    private final ChangeOwnPassword changeOwnPassword;

    public ChangeOwnPasswordRoute(ChangeOwnPassword changeOwnPassword) {
        this.changeOwnPassword = changeOwnPassword;
    }

    @Operation(summary = "Cambiar mi contraseña", description = "Exige la contraseña actual")
    @PutMapping("/api/v1/users/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@Valid @RequestBody ChangePasswordRequest request) {
        changeOwnPassword.execute(request);
    }
}
