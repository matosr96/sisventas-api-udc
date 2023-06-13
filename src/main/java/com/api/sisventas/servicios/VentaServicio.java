package com.api.sisventas.servicios;

import com.api.sisventas.entidades.Venta;
import com.api.sisventas.repositorios.VentaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VentaServicio {
    private VentaRepositorio ventaRepositorio;

    @Autowired
    public VentaServicio(VentaRepositorio ventaRepositorio){
        this.ventaRepositorio = ventaRepositorio;
    }

    public void generarVenta(Venta venta){
        ventaRepositorio.save(venta);
    }

    public List<Venta> obtenerVentas() {
        try {
            return ventaRepositorio.findAll();
        } catch (Exception e) {
            System.out.println("Error al obtener todos las ventas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Optional<Venta> obtenerVentaPorId(Long id){
        return ventaRepositorio.findById(id);
    }

    public void eliminarVenta(Long id){
         ventaRepositorio.deleteById(id);
    }

    public Venta actualizarVenta(Long id, Venta ventaActualizada) {
        Optional<Venta> ventaExistente = ventaRepositorio.findById(id);
        if (ventaExistente.isPresent()) {
            Venta venta = ventaExistente.get();
            venta.setFechaVenta(ventaActualizada.getFechaVenta());
            venta.setTotalVenta(ventaActualizada.getTotalVenta());
            return ventaRepositorio.save(venta);
        } else {
            throw new RuntimeException("No se encontró la venta con el ID especificado");
        }
    }
}
