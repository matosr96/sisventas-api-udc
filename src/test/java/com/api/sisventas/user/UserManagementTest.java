package com.api.sisventas.user;

import com.api.sisventas.businessLogic.user.ChangeOwnPassword;
import com.api.sisventas.businessLogic.user.UpdateUserRoles;
import com.api.sisventas.businessLogic.user.UpdateUserStatus;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.UserStatus;
import com.api.sisventas.models.dtos.user.ChangePasswordRequest;
import com.api.sisventas.models.dtos.user.UpdateUserRolesRequest;
import com.api.sisventas.models.dtos.user.UpdateUserStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Salvaguardas de la gestión de usuarios: nadie se bloquea a sí mismo y una cuenta inactiva no entra. */
@SpringBootTest
class UserManagementTest {

    private static final String ADMIN = "um-admin";
    private static final String OTHER = "um-other";

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private UpdateUserRoles updateUserRoles;
    @Autowired private UpdateUserStatus updateUserStatus;
    @Autowired private ChangeOwnPassword changeOwnPassword;

    private Long adminId;
    private Long otherId;

    @BeforeEach
    void setUp() {
        Role admin = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
        Role user = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.USER)));
        adminId = userRepository.findByUsername(ADMIN).orElseGet(() -> save(ADMIN, Set.of(admin))).getId();
        otherId = userRepository.findByUsername(OTHER).orElseGet(() -> save(OTHER, Set.of(user))).getId();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ADMIN, null, List.of()));
    }

    private User save(String username, Set<Role> roles) {
        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setRoles(roles);
        return userRepository.save(u);
    }

    @Test
    void adminCannotRemoveOwnAdminRoleNorDeactivateSelf() {
        DomainError roles = assertThrows(DomainError.class,
                () -> updateUserRoles.execute(adminId, new UpdateUserRolesRequest(Set.of(RoleName.USER))));
        assertEquals(ErrorCodes.CANNOT_MODIFY_SELF, roles.code());

        DomainError status = assertThrows(DomainError.class,
                () -> updateUserStatus.execute(adminId, new UpdateUserStatusRequest(UserStatus.INACTIVE)));
        assertEquals(ErrorCodes.CANNOT_MODIFY_SELF, status.code());
    }

    @Test
    void deactivatedUserIsDisabledForAuthentication() {
        updateUserStatus.execute(otherId, new UpdateUserStatusRequest(UserStatus.INACTIVE));
        UserDetails details = userDetailsService.loadUserByUsername(OTHER);
        assertFalse(details.isEnabled(), "una cuenta INACTIVE no debe poder autenticarse");

        updateUserStatus.execute(otherId, new UpdateUserStatusRequest(UserStatus.ACTIVE));
        assertTrue(userDetailsService.loadUserByUsername(OTHER).isEnabled());
    }

    @Test
    void promotingAnotherUserWorksAndPasswordChangeRequiresCurrentOne() {
        assertTrue(updateUserRoles.execute(otherId, new UpdateUserRolesRequest(Set.of(RoleName.USER, RoleName.ADMIN)))
                .roles().contains(RoleName.ADMIN));

        DomainError wrong = assertThrows(DomainError.class,
                () -> changeOwnPassword.execute(new ChangePasswordRequest("not-the-password", "new-password-1")));
        assertEquals(ErrorCodes.INVALID_CURRENT_PASSWORD, wrong.code());

        changeOwnPassword.execute(new ChangePasswordRequest("password123", "new-password-1"));
        String stored = userRepository.findByUsername(ADMIN).orElseThrow().getPassword();
        assertTrue(passwordEncoder.matches("new-password-1", stored));
    }
}
