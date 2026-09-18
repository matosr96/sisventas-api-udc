package com.api.sisventas.businessLogic.auth;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.SqlErrors;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.auth.AuthResponse;
import com.api.sisventas.models.dtos.auth.SignupRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import com.api.sisventas.security.JwtGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SignupUser {

    private static final RoleName DEFAULT_ROLE = RoleName.USER;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;

    public SignupUser(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtGenerator jwtGenerator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
    }

    @Transactional
    public AuthResponse execute(SignupRequest request) {
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new DomainError(ErrorCodes.DEFAULT_ROLE_MISSING));

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoto(request.photo());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));

        User saved = save(user);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return AuthResponse.of(jwtGenerator.generateToken(authentication), UserResponse.from(saved));
    }

    /** El unique de {@code users.username} es la única verificación: no se consulta antes. */
    private User save(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException error) {
            if (SqlErrors.isUniqueViolation(error)) {
                throw new DomainError(ErrorCodes.USERNAME_ALREADY_EXISTS);
            }
            throw error;
        }
    }
}
