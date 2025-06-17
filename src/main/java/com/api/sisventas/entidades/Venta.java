package com.api.sisventas.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="sales")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale")
    private Long idSale;

    @Column(name = "fecha_venta")
    private Date fechaVenta;

    @Column(name = "total_venta")
    private Integer totalVenta;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "venta_product_ids",
        joinColumns = @JoinColumn(name = "venta_id_sale"),
        inverseJoinColumns = @JoinColumn(name = "product_ids")
    )
    private List<Producto> productos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id_usuario")
    private Usuarios usuario;
}
