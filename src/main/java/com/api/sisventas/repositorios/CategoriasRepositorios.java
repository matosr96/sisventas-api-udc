package com.api.sisventas.repositorios;

import com.api.sisventas.entidades.Categorias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriasRepositorios extends JpaRepository<Categorias, Long> {
}
