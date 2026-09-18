package com.api.sisventas.businessLogic.user;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cierra la sesión en todos los dispositivos: sube la versión y ningún token emitido vale ya, este incluido. */
@Service
public class LogoutEverywhere {

    private final UserRepository userRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public LogoutEverywhere(UserRepository userRepository, GetAuthenticatedUser getAuthenticatedUser) {
        this.userRepository = userRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public void execute() {
        User user = getAuthenticatedUser.execute();
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }
}
