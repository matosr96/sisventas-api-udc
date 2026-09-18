package com.api.sisventas.businessLogic.user;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.models.dtos.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El propio perfil, resuelto desde el token: nunca desde un id enviado por el cliente. */
@Service
public class GetCurrentUser {

    private final GetAuthenticatedUser getAuthenticatedUser;

    public GetCurrentUser(GetAuthenticatedUser getAuthenticatedUser) {
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional(readOnly = true)
    public UserResponse execute() {
        return UserResponse.from(getAuthenticatedUser.execute());
    }
}
