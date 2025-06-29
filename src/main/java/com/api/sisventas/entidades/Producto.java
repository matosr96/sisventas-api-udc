package com.api.sisventas.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long idProduct;

    @Column(name = "name")
    private String name;

    @Column(name = "purchase_price")
    private Double purchasePrice;

    @Column(name = "sale_price")
    private Double salePrice;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "initial_stock")
    private Integer initialStock;

    @Column(name = "status_product")
    private String statusProduct;

    @Column(name = "image")
    private String image;

    @Column(name = "low")
    private Integer low;

    @Column(name = "sales")
    private Integer sales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category", referencedColumnName = "id_category")
    private Categorias categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", referencedColumnName = "id_usuario")
    private Usuarios usuario;
}
