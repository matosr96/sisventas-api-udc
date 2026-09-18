package com.api.sisventas.businessLogic.user;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.user.UserResponse;
import com.api.sisventas.models.dtos.user.UpdateUserStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Activa o desactiva una cuenta. Nadie se desactiva a sí mismo: se quedaría fuera al instante. */
@Service
public class UpdateUserStatus {

    private final UserRepository userRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public UpdateUserStatus(UserRepository userRepository, GetAuthenticatedUser getAuthenticatedUser) {
        this.userRepository = userRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public UserResponse execute(Long id, UpdateUserStatusRequest request) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.USER_NOT_FOUND));
        if (target.getId().equals(getAuthenticatedUser.execute().getId())) {
            throw new DomainError(ErrorCodes.CANNOT_MODIFY_SELF);
        }
        target.setStatus(request.status());
        return UserResponse.from(userRepository.save(target));
    }
}
