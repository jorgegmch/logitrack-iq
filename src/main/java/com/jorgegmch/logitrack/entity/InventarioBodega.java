package com.jorgegmch.logitrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventario_bodega", uniqueConstraints = 
    @UniqueConstraint(columnNames = {"bodega_id", "producto_id"}))
@Data
@EqualsAndHashCode(of = "idInvBodega")
@NoArgsConstructor
@AllArgsConstructor
public class InventarioBodega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInvBodega;

    @ManyToOne
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodegaId;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto productoId;

    @Column(nullable = false)
    private Integer stock;
}
