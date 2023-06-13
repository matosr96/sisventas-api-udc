package com.api.sisventas.servicios;

import com.api.sisventas.entidades.Producto;
import com.api.sisventas.repositorios.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicio {

    private ProductoRepositorio productoRepositorio;

    @Autowired
    public ProductoServicio(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    public void crearProducto(Producto producto) {
        productoRepositorio.save(producto);
    }

    public List<Producto> getAllProducts() {
        try {
            return productoRepositorio.findAll();
        } catch (Exception e) {
            System.out.println("Error al obtener todos los productos: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepositorio.findById(id);
    }

    public void actualizarProducto(Long id, Producto producto) {
        Optional<Producto> productoExistente = productoRepositorio.findById(id);

        if (productoExistente.isPresent()) {
            Producto productoActualizado = productoExistente.get();
            // Actualizar los valores del producto con los nuevos valores
            productoActualizado.setName(producto.getName());
            productoActualizado.setPurchasePrice(producto.getPurchasePrice());
            productoActualizado.setSalePrice(producto.getSalePrice());
            productoActualizado.setCurrentStock(producto.getCurrentStock());
            productoActualizado.setInitialStock(producto.getInitialStock());
            productoActualizado.setStatusProduct(producto.getStatusProduct());
            productoActualizado.setCategory(producto.getCategory());
            productoActualizado.setImage(producto.getImage());
            productoActualizado.setLow(producto.getLow());
            productoActualizado.setSales(producto.getSales());
            // Guardar el producto actualizado
            productoRepositorio.save(productoActualizado);
        } else {
            // Manejar el caso en el que no se encuentre el producto con el ID dado
            throw new RuntimeException("No se encontró el producto con el ID especificado");
        }
    }

    public void eliminarProducto(Long id) {
        productoRepositorio.deleteById(id);
    }
}
