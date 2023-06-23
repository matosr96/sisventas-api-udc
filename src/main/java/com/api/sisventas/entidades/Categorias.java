package com.api.sisventas.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categories")
public class Categorias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_category")
    private Long idCategory;

    @Column(name = "name_category")
    private String name;

    @Column(name = "icon_category")
    private String icon;

    @Column(name = "user")
    private Number user;
}
