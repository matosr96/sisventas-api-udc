package com.api.sisventas.routes.user;

import com.api.sisventas.businessLogic.user.GetUser;
import com.api.sisventas.models.dtos.auth.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
public class GetUserRoute {

    private final GetUser getUser;

    public GetUserRoute(GetUser getUser) {
        this.getUser = getUser;
    }

    @Operation(summary = "Obtener usuario", description = "Devuelve un usuario por su id; solo ADMIN")
    @GetMapping("/api/v1/users/{id}")
    public UserResponse handle(@PathVariable Long id) {
        return getUser.execute(id);
    }
}
