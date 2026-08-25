package com.jorgegmch.logitrack.dto;

public class StockPorBodegaDTO {

    private Long bodegaId;
    private String nombreBodega;
    private Long stockTotal;

    public StockPorBodegaDTO(Long bodegaId, String nombreBodega, Long stockTotal) {
        this.bodegaId = bodegaId;
        this.nombreBodega = nombreBodega;
        this.stockTotal = stockTotal;
    }

    public Long getBodegaId() {
        return bodegaId;
    }

    public String getNombreBodega() {
        return nombreBodega;
    }

    public Long getStockTotal() {
        return stockTotal;
    }
}
