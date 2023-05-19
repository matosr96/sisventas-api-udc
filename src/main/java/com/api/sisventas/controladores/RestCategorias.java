package com.api.sisventas.controladores;

import com.api.sisventas.entidades.Categorias;
import com.api.sisventas.servicios.CategoriasServicios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/categories")
public class RestCategorias {

    private CategoriasServicios categoriasServicios;

    @Autowired
    public RestCategorias(CategoriasServicios categoriasServicios){this.categoriasServicios = categoriasServicios;}

    @PostMapping(value = "/crear", headers = "Accept=application/json")
    public void crearCategoriaRoute(@RequestBody Categorias categorias){categoriasServicios.crearCategoria(categorias);}

    @GetMapping(value = "/listar", headers = "Accept=application/json")
    public List<Categorias> listarCategoriasRoute(){return categoriasServicios.readAllCategorias();}

    @GetMapping(value = "/listarId/{id}", headers = "Accept=application/json")
    public Optional<Categorias> ObtenerCategoriaRoute(@PathVariable Long id){return categoriasServicios.readOneCategoria(id);}

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarCategoriaRoute(@PathVariable("id") Long id, @RequestBody Categorias categorias) {
        try {
            categorias.setIdCategory(id);
            categoriasServicios.updateCategoria(id, categorias);
            return ResponseEntity.ok("Categoría actualizada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo actualizar la categoría");
        }
    }

    @DeleteMapping(value = "/eliminar/{id}", headers = "Accept=application/json")
    public void eliminarCategoria(@PathVariable Long id) {
        categoriasServicios.deleteCategoria(id);
    }
}
