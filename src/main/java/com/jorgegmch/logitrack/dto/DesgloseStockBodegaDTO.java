package com.jorgegmch.logitrack.dto;

public class DesgloseStockBodegaDTO {

    private Long bodegaId;
    private String nombreBodega;
    private Long stock;

    public DesgloseStockBodegaDTO(Long bodegaId, String nombreBodega, Long stock) {
        this.bodegaId = bodegaId;
        this.nombreBodega = nombreBodega;
        this.stock = stock;
    }

    public Long getBodegaId() {
        return bodegaId;
    }

    public String getNombreBodega() {
        return nombreBodega;
    }

    public Long getStock() {
        return stock;
    }
}