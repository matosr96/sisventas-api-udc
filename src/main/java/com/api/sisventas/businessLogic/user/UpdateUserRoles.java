package com.api.sisventas.businessLogic.user;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.auth.UserResponse;
import com.api.sisventas.models.dtos.user.UpdateUserRolesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Reemplaza los roles de otro usuario. Nadie se quita el ADMIN a sí mismo: si fuera el
 * último administrador, el sistema quedaría sin nadie que pudiera devolvérselo.
 */
@Service
public class UpdateUserRoles {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;

    public UpdateUserRoles(UserRepository userRepository,
                           RoleRepository roleRepository,
                           GetAuthenticatedUser getAuthenticatedUser) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
    }

    @Transactional
    public UserResponse execute(Long id, UpdateUserRolesRequest request) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.USER_NOT_FOUND));
        User actor = getAuthenticatedUser.execute();
        if (target.getId().equals(actor.getId()) && !request.roles().contains(RoleName.ADMIN)) {
            throw new DomainError(ErrorCodes.CANNOT_MODIFY_SELF);
        }
        Set<Role> roles = new HashSet<>();
        for (RoleName name : request.roles()) {
            roles.add(roleRepository.findByName(name)
                    .orElseThrow(() -> new DomainError(ErrorCodes.ROLE_NOT_FOUND)));
        }
        target.setRoles(roles);
        return UserResponse.from(userRepository.save(target));
    }
}
