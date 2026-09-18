package com.api.sisventas.businessLogic.user;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.user.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Solo la propia contraseña, y solo demostrando la actual: un token robado no debe bastar. */
@Service
public class ChangeOwnPassword {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public ChangeOwnPassword(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             GetAuthenticatedUser getAuthenticatedUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public void execute(ChangePasswordRequest request) {
        User user = getAuthenticatedUser.execute();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new DomainError(ErrorCodes.INVALID_CURRENT_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
