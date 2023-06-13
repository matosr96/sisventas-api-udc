package com.api.sisventas.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name="sales")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSale")
    private Long idSale;

    @Column(name = "fechaVenta")
    private Date fechaVenta;

    @Column(name = "totalVenta")
    private Integer totalVenta;

    @Column(name = "productIds")
    @ElementCollection
    private Long[] productIds;

    @Column(name = "userId")
    private Long userId;
}
