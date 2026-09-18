package com.api.sisventas.businessLogic.auth;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.auth.AuthResponse;
import com.api.sisventas.models.dtos.auth.SigninRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import com.api.sisventas.security.JwtGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SigninUser {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;

    public SigninUser(UserRepository userRepository,
                      AuthenticationManager authenticationManager,
                      JwtGenerator jwtGenerator) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(SigninRequest request) {
        Authentication authentication = authenticate(request);
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new DomainError(ErrorCodes.INVALID_CREDENTIALS));
        return AuthResponse.of(jwtGenerator.generateToken(authentication), UserResponse.from(user));
    }

    private Authentication authenticate(SigninRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException error) {
            throw new DomainError(ErrorCodes.INVALID_CREDENTIALS);
        }
    }
}
