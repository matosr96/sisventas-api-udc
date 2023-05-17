package com.api.sisventas.servicios;

import com.api.sisventas.entidades.Categorias;
import com.api.sisventas.repositorios.CategoriasRepositorios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriasServicios {
    private CategoriasRepositorios categoriasRepositorios;

    @Autowired
    public CategoriasServicios(CategoriasRepositorios categoriasRepositorios){this.categoriasRepositorios = categoriasRepositorios;}


    public void crearCategoria(Categorias categorias) {
        categoriasRepositorios.save(categorias);
    }

    public List<Categorias> readAllCategorias() {
        return categoriasRepositorios.findAll();
    }

    public Optional<Categorias> readOneCategoria(Long id) {
        return categoriasRepositorios.findById(id);
    }

    public void updateCategoria(Categorias categorias) {
        categoriasRepositorios.save(categorias);
    }

    public void deleteCategoria(Long id) {
        categoriasRepositorios.deleteById(id);
    }
}
