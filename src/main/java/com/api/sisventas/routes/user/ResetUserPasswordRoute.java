package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.ResetUserPassword;
import com.api.sisventas.models.dtos.user.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class ResetUserPasswordRoute {

    private final ResetUserPassword resetUserPassword;

    public ResetUserPasswordRoute(ResetUserPassword resetUserPassword) {
        this.resetUserPassword = resetUserPassword;
    }

    @Operation(summary = "Reset password",
            description = "ADMIN sets a new password; the user's sessions are closed")
    @PutMapping("/api/v1/users/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        resetUserPassword.execute(id, request);
    }
}
