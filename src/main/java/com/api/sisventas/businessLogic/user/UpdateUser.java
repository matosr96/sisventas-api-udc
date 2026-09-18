package com.api.sisventas.businessLogic.user;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.user.UpdateUserRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Datos personales de cualquier usuario (ADMIN) o los propios (vía {@link UpdateOwnProfile}). */
@Service
public class UpdateUser {

    private final UserRepository userRepository;

    public UpdateUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse execute(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.USER_NOT_FOUND));
        return UserResponse.from(apply(user, request));
    }

    User apply(User user, UpdateUserRequest request) {
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.photo() != null) {
            user.setPhoto(request.photo());
        }
        return userRepository.save(user);
    }
}
