package com.api.sisventas.models.dtos.auth;

import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.UserStatus;

import java.time.Instant;
import java.util.List;

/** Contrato público del usuario. No incluye {@code password} por diseño. */
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String photo,
        String username,
        List<RoleName> roles,
        UserStatus status,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoto(),
                user.getUsername(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.getStatus(),
                user.getCreatedAt());
    }
}
