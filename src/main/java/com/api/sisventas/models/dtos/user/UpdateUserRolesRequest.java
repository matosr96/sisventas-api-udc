package com.api.sisventas.models.dtos.user;

import com.api.sisventas.models.RoleName;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/** Reemplaza el conjunto de roles completo. Sin roles no hay cuenta útil, así que no puede ir vacío. */
public record UpdateUserRolesRequest(@NotEmpty Set<RoleName> roles) {
}
