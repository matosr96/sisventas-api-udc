package com.api.sisventas.models.dtos.user;

import com.api.sisventas.models.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {
}
