package com.api.sisventas.controladores;

import com.api.sisventas.entidades.Categorias;
import com.api.sisventas.servicios.CategoriasServicios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/categories")
@Tag(name = "Categorías", description = "APIs para gestión de categorías de productos")
public class RestCategorias {

    private CategoriasServicios categoriasServicios;

    @Autowired
    public RestCategorias(CategoriasServicios categoriasServicios) {
        this.categoriasServicios = categoriasServicios;
    }

    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de productos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de la categoría inválidos"),
        @ApiResponse(responseCode = "403", description = "No autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/crear", headers = "Accept=application/json")
    public ResponseEntity<Void> crearCategoriaRoute(
            @Parameter(description = "Datos de la categoría a crear", required = true)
            @RequestBody Categorias categorias) {
        try {
            categoriasServicios.crearCategoria(categorias);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Listar categorías", description = "Obtiene la lista de todas las categorías")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Categorias.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "/listar", headers = "Accept=application/json")
    public ResponseEntity<List<Categorias>> listarCategoriasRoute() {
        try {
            List<Categorias> categorias = categoriasServicios.readAllCategorias();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = Categorias.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "/listarId/{id}", headers = "Accept=application/json")
    public ResponseEntity<Categorias> obtenerCategoriaRoute(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable Long id) {
        try {
            Optional<Categorias> categoria = categoriasServicios.readOneCategoria(id);
            return categoria.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de la categoría inválidos"),
        @ApiResponse(responseCode = "403", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Void> actualizarCategoriaRoute(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "Datos actualizados de la categoría", required = true)
            @RequestBody Categorias categorias) {
        try {
            if (!categoriasServicios.readOneCategoria(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            categorias.setIdCategory(id);
            categoriasServicios.updateCategoria(id, categorias);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping(value = "/eliminar/{id}", headers = "Accept=application/json")
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable Long id) {
        try {
            if (!categoriasServicios.readOneCategoria(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            categoriasServicios.deleteCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
