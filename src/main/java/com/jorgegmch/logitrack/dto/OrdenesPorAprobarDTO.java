package com.jorgegmch.logitrack.dto;

import java.math.BigDecimal;

public class OrdenesPorAprobarDTO {

    private Long cantidad;
    private BigDecimal montoTotal;

    public OrdenesPorAprobarDTO(Long cantidad, BigDecimal montoTotal) {
        this.cantidad = cantidad;
        this.montoTotal = montoTotal;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
}