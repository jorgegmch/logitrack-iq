package com.jorgegmch.logitrack.dto;

import java.util.List;

public class ReporteResumenResponse {

    private List<StockPorBodegaDTO> stockTotalPorBodega;
    private List<ProductoMasMovidoDTO> productosMasMovidos;

    public ReporteResumenResponse(List<StockPorBodegaDTO> stockTotalPorBodega, List<ProductoMasMovidoDTO> productosMasMovidos) {
        this.stockTotalPorBodega = stockTotalPorBodega;
        this.productosMasMovidos = productosMasMovidos;
    }

    public List<StockPorBodegaDTO> getStockTotalPorBodega() {
        return stockTotalPorBodega;
    }

    public List<ProductoMasMovidoDTO> getProductosMasMovidos() {
        return productosMasMovidos;
    }
}
