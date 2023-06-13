package com.api.sisventas.controladores;

import com.api.sisventas.entidades.Venta;
import com.api.sisventas.servicios.VentaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/sales")
public class RestVentas {

    private VentaServicio ventaServicio;

    @Autowired
    public RestVentas(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    @PostMapping(value = "/create", headers = "Accept=application/json")
    public void generarVentaRoute(@RequestBody Venta venta) {
        ventaServicio.generarVenta(venta);
    }

    @GetMapping(value = "/getById/{id}", headers = "Accept=application/json")
    public Optional<Venta> obtenerVentaRoute(@PathVariable Long id) {
        return ventaServicio.obtenerVentaPorId(id);
    }

    @GetMapping(value = "/getAll", headers = "Accept=application/json")
    public ResponseEntity<List<Venta>> obtenerVentasRoute() {
        try {
            List<Venta> ventas = ventaServicio.obtenerVentas();
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            System.out.println("Error al obtener todas las ventas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> actualizarVentaRoute(@PathVariable("id") Long id, @RequestBody Venta venta) {
        try {
            venta.setIdSale(id);
            ventaServicio.actualizarVenta(id, venta);
            return ResponseEntity.ok("Venta actualizada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo actualizar la venta");
        }
    }

    @DeleteMapping(value = "/delete/{id}", headers = "Accept=application/json")
    public void eliminarVenta(@PathVariable Long id) {
        ventaServicio.eliminarVenta(id);
    }
}
