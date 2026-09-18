package com.api.sisventas.businessLogic.user;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.models.dtos.user.UpdateUserRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El propio perfil: la identidad la pone el token, nunca un id del body. */
@Service
public class UpdateOwnProfile {

    private final UpdateUser updateUser;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public UpdateOwnProfile(UpdateUser updateUser, GetAuthenticatedUser getAuthenticatedUser) {
        this.updateUser = updateUser;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public UserResponse execute(UpdateUserRequest request) {
        return UserResponse.from(updateUser.apply(getAuthenticatedUser.execute(), request));
    }
}
