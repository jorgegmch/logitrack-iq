package com.jorgegmch.logitrack.dto;

import java.math.BigDecimal;

public class OcupacionBodegaDTO {

    private Long bodegaId;
    private String nombre;
    private BigDecimal porcentaje;

    public OcupacionBodegaDTO(Long bodegaId, String nombre, BigDecimal porcentaje) {
        this.bodegaId = bodegaId;
        this.nombre = nombre;
        this.porcentaje = porcentaje;
    }

    public Long getBodegaId() {
        return bodegaId;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }
}