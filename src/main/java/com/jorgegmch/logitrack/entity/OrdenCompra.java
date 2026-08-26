package com.jorgegmch.logitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.listener.AuditoriaListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "orden_compra")
@Data
@EqualsAndHashCode(of = "idOrdenCompra")
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrdenCompra;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto productoId;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedorId;

    @ManyToOne
    @JoinColumn(name = "bodega_destino_id", nullable = false)
    private Bodega bodegaDestinoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @ManyToOne
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPorId;

    @Column(name = "pdf_generado")
    private byte[] pdfGenerado;

    @Column(name = "fecha_generacion_pdf")
    private LocalDateTime fechaGeneracionPdf;
}