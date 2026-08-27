package com.jorgegmch.logitrack.dto;

public class ProductoStockResponse {

    private Long productoId;
    private Long stockTotal;

    public ProductoStockResponse(Long productoId, Long stockTotal) {
        this.productoId = productoId;
        this.stockTotal = stockTotal;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Long getStockTotal() {
        return stockTotal;
    }
}