package com.api.sisventas.user;

import com.api.sisventas.businessLogic.user.CreateUser;
import com.api.sisventas.businessLogic.user.ListUsers;
import com.api.sisventas.businessLogic.user.LogoutEverywhere;
import com.api.sisventas.businessLogic.user.ResetUserPassword;
import com.api.sisventas.businessLogic.user.UpdateOwnProfile;
import com.api.sisventas.businessLogic.user.UpdateUser;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.UserStatus;
import com.api.sisventas.models.dtos.user.CreateUserRequest;
import com.api.sisventas.models.dtos.user.ResetPasswordRequest;
import com.api.sisventas.models.dtos.user.UpdateUserRequest;
import com.api.sisventas.models.dtos.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Alta por administrador, datos personales, reinicio de contraseña y versión de token. */
@SpringBootTest
class UserAccountsTest {

    private static final String ADMIN = "accounts-admin";

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CreateUser createUser;
    @Autowired private UpdateUser updateUser;
    @Autowired private UpdateOwnProfile updateOwnProfile;
    @Autowired private ResetUserPassword resetUserPassword;
    @Autowired private LogoutEverywhere logoutEverywhere;
    @Autowired private ListUsers listUsers;

    @BeforeEach
    void setUp() {
        Role admin = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
        roleRepository.findByName(RoleName.USER).orElseGet(() -> roleRepository.save(new Role(null, RoleName.USER)));
        if (userRepository.findByUsername(ADMIN).isEmpty()) {
            User user = new User();
            user.setFirstName("Accounts");
            user.setLastName("Admin");
            user.setUsername(ADMIN);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRoles(Set.of(admin));
            userRepository.save(user);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ADMIN, null, List.of()));
    }

    private CreateUserRequest request(String username, RoleName role) {
        return new CreateUserRequest("Nueva", "Cuenta", null, username, "password123", role);
    }

    @Test
    void adminCreatesAccountsWithARoleAndUniqueUsername() {
        String username = "acc-" + System.nanoTime();
        UserResponse seller = createUser.execute(request(username, null));
        assertEquals(List.of(RoleName.USER), seller.roles(), "sin rol explícito nace vendedor");
        UserResponse boss = createUser.execute(request(username + "-b", RoleName.ADMIN));
        assertEquals(List.of(RoleName.ADMIN), boss.roles());
        assertEquals(UserStatus.ACTIVE, boss.status());

        DomainError duplicate = assertThrows(DomainError.class, () -> createUser.execute(request(username, null)));
        assertEquals(ErrorCodes.USERNAME_ALREADY_EXISTS, duplicate.code());
        assertEquals(1, listUsers.execute(username + "-b", UserStatus.ACTIVE, "username", "asc", 1, 10).count());
    }

    @Test
    void profileUpdatesArePartialAndOwnProfileComesFromTheToken() {
        UserResponse created = createUser.execute(request("prof-" + System.nanoTime(), null));
        UserResponse renamed = updateUser.execute(created.id(), new UpdateUserRequest("Renombrada", null, "https://x/y.png"));
        assertEquals("Renombrada", renamed.firstName());
        assertEquals("Cuenta", renamed.lastName(), "nulo = no tocar");
        assertEquals("https://x/y.png", renamed.photo());

        UserResponse me = updateOwnProfile.execute(new UpdateUserRequest(null, "Propio", null));
        assertEquals(ADMIN, me.username());
        assertEquals("Propio", me.lastName());
        DomainError missing = assertThrows(DomainError.class,
                () -> updateUser.execute(999_999L, new UpdateUserRequest("x", null, null)));
        assertEquals(ErrorCodes.USER_NOT_FOUND, missing.code());
    }

    @Test
    void passwordResetAndLogoutEverywhereBumpTheTokenVersion() {
        UserResponse target = createUser.execute(request("reset-" + System.nanoTime(), null));
        int before = userRepository.findById(target.id()).orElseThrow().getTokenVersion();
        resetUserPassword.execute(target.id(), new ResetPasswordRequest("otra-clave-123"));
        User after = userRepository.findById(target.id()).orElseThrow();
        assertEquals(before + 1, after.getTokenVersion());
        assertTrue(passwordEncoder.matches("otra-clave-123", after.getPassword()));

        int mine = userRepository.findByUsername(ADMIN).orElseThrow().getTokenVersion();
        logoutEverywhere.execute();
        assertEquals(mine + 1, userRepository.findByUsername(ADMIN).orElseThrow().getTokenVersion());
    }
}
