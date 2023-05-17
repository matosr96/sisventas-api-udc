package com.api.sisventas.repositorios;

import com.api.sisventas.entidades.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepositorio extends JpaRepository<Roles, Long> {
    Optional<Roles> findByName(String name);
}
