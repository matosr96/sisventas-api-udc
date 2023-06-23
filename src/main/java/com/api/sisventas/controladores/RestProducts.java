package com.api.sisventas.controladores;

import com.api.sisventas.entidades.Producto;
import com.api.sisventas.servicios.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/products")
public class RestProducts {

    private ProductoServicio productoServicio;

    @Autowired
    public RestProducts(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @PostMapping(value = "/create", headers = "Accept=application/json")
    public void crearProductoRoute(@RequestBody Producto producto) {
        productoServicio.crearProducto(producto);
    }

    @GetMapping(value = "/getAll", headers = "Accept=application/json")
    public ResponseEntity<List<Producto>> getAllProductsRoute() {
        try {
            List<Producto> productos = productoServicio.getAllProducts();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.out.println("Error al obtener todos los productos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/getById/{id}", headers = "Accept=application/json")
    public Optional<Producto> obtenerProductoRoute(@PathVariable Long id) {
        return productoServicio.obtenerProductoPorId(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> actualizarProductoRoute(@PathVariable("id") Long id, @RequestBody Producto producto) {
        try {
            System.out.println("ID del producto ===================================================: " + id);
            System.out.println("Producto ===================================================:: " + producto);
            producto.setIdProduct(id);
            productoServicio.actualizarProducto(id, producto);
            return ResponseEntity.ok("Producto actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo actualizar el producto");
        }
    }

    @DeleteMapping(value = "/delete/{id}", headers = "Accept=application/json")
    public void eliminarProducto(@PathVariable Long id) {
        productoServicio.eliminarProducto(id);
    }
}
