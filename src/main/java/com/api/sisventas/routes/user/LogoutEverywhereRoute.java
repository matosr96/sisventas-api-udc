package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.LogoutEverywhere;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class LogoutEverywhereRoute {

    private final LogoutEverywhere logoutEverywhere;

    public LogoutEverywhereRoute(LogoutEverywhere logoutEverywhere) {
        this.logoutEverywhere = logoutEverywhere;
    }

    @Operation(summary = "Log out everywhere", description = "Invalidates every token issued to the current user")
    @PostMapping("/api/v1/users/me/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle() {
        logoutEverywhere.execute();
    }
}
