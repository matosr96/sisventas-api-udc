package com.api.sisventas.businessLogic.user;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.dtos.auth.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUser {

    private final UserRepository userRepository;

    public GetUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse execute(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new DomainError(ErrorCodes.USER_NOT_FOUND));
    }
}
