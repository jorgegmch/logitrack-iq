package com.jorgegmch.logitrack.dto;

import java.time.ZonedDateTime;
import java.util.List;

public class KpiResponse {

    private ZonedDateTime calculadoEn;
    private List<OcupacionBodegaDTO> ocupacionPorBodega;
    private Long productosEnQuiebre;
    private Long productosEnRiesgo;
    private OrdenesPorAprobarDTO ordenesPorAprobar;
    private MovimientosAyerDTO movimientosAyer;

    public KpiResponse(ZonedDateTime calculadoEn, List<OcupacionBodegaDTO> ocupacionPorBodega,
            Long productosEnQuiebre, Long productosEnRiesgo, OrdenesPorAprobarDTO ordenesPorAprobar,
            MovimientosAyerDTO movimientosAyer) {
        this.calculadoEn = calculadoEn;
        this.ocupacionPorBodega = ocupacionPorBodega;
        this.productosEnQuiebre = productosEnQuiebre;
        this.productosEnRiesgo = productosEnRiesgo;
        this.ordenesPorAprobar = ordenesPorAprobar;
        this.movimientosAyer = movimientosAyer;
    }

    public ZonedDateTime getCalculadoEn() {
        return calculadoEn;
    }

    public List<OcupacionBodegaDTO> getOcupacionPorBodega() {
        return ocupacionPorBodega;
    }

    public Long getProductosEnQuiebre() {
        return productosEnQuiebre;
    }

    public Long getProductosEnRiesgo() {
        return productosEnRiesgo;
    }

    public OrdenesPorAprobarDTO getOrdenesPorAprobar() {
        return ordenesPorAprobar;
    }

    public MovimientosAyerDTO getMovimientosAyer() {
        return movimientosAyer;
    }
}