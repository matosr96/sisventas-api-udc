package com.api.sisventas.repositorios;

import com.api.sisventas.entidades.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuariosRepositorios extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByUsername(String username);

    Boolean existsByUsername(String username);
}
