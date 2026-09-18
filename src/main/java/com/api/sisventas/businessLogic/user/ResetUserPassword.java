package com.api.sisventas.businessLogic.user;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.user.ResetPasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reinicio de contraseña por un administrador (no hay correo para recuperarla sola).
 * Sube la versión de token: quien tuviera la cuenta abierta con la contraseña vieja sale.
 */
@Service
public class ResetUserPassword {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetUserPassword(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(Long id, ResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }
}
