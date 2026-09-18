package com.api.sisventas.businessLogic.auth;

import com.api.sisventas.common.Authenticated;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resuelve el usuario dueño de la petición a partir del contexto de seguridad.
 * Única puerta de entrada a la identidad dentro de la lógica de negocio.
 */
@Service
public class GetAuthenticatedUser {

    private final UserRepository userRepository;

    public GetAuthenticatedUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User execute() {
        return userRepository.findByUsername(Authenticated.username())
                .orElseThrow(() -> new DomainError(ErrorCodes.USER_NOT_FOUND));
    }
}
