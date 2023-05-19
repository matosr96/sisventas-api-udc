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

    public void updateCategoria(Long id, Categorias categorias) {
        Optional<Categorias> categoriaExistente = categoriasRepositorios.findById(id);
        if (categoriaExistente.isPresent()) {
            Categorias categoria = categoriaExistente.get();
            // Actualizar los valores de la categoría con los nuevos valores
            categoria.setName(categorias.getName());
            categoria.setIcon(categorias.getIcon());
            // Guardar la categoría actualizada
            categoriasRepositorios.save(categoria);
        } else {
            // Manejar el caso en el que no se encuentre la categoría con el ID dado
            throw new RuntimeException("No se encontró la categoría con el ID especificado");
        }
    }

    public void deleteCategoria(Long id) {
        categoriasRepositorios.deleteById(id);
    }
}
