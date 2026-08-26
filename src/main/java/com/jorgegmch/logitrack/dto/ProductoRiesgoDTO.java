package com.jorgegmch.logitrack.dto;

import java.math.BigDecimal;

public class ProductoRiesgoDTO {

    private Long productoId;
    private String nombreProducto;
    private Long proveedorId;
    private Long stockTotal;
    private BigDecimal consumoDiarioPromedio;
    private BigDecimal puntoReorden;
    private BigDecimal diasCobertura;
    private String estadoCobertura;
    private Long bodegaDestinoId;

    public ProductoRiesgoDTO(Long productoId, String nombreProducto, Long proveedorId, Long stockTotal,
            BigDecimal consumoDiarioPromedio, BigDecimal puntoReorden, BigDecimal diasCobertura,
            String estadoCobertura, Long bodegaDestinoId) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.proveedorId = proveedorId;
        this.stockTotal = stockTotal;
        this.consumoDiarioPromedio = consumoDiarioPromedio;
        this.puntoReorden = puntoReorden;
        this.diasCobertura = diasCobertura;
        this.estadoCobertura = estadoCobertura;
        this.bodegaDestinoId = bodegaDestinoId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public Long getStockTotal() {
        return stockTotal;
    }

    public BigDecimal getConsumoDiarioPromedio() {
        return consumoDiarioPromedio;
    }

    public BigDecimal getPuntoReorden() {
        return puntoReorden;
    }

    public BigDecimal getDiasCobertura() {
        return diasCobertura;
    }

    public String getEstadoCobertura() {
        return estadoCobertura;
    }

    public Long getBodegaDestinoId() {
        return bodegaDestinoId;
    }
}