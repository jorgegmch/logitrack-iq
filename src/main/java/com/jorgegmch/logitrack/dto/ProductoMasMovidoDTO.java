package com.jorgegmch.logitrack.dto;

public class ProductoMasMovidoDTO {

    private Long productoId;
    private String nombreProducto;
    private Long cantidadTotalMovida;

    public ProductoMasMovidoDTO(Long productoId, String nombreProducto, Long cantidadTotalMovida) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidadTotalMovida = cantidadTotalMovida;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Long getCantidadTotalMovida() {
        return cantidadTotalMovida;
    }
}
