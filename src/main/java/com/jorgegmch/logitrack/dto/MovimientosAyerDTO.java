package com.jorgegmch.logitrack.dto;

public class MovimientosAyerDTO {

    private Long entrada;
    private Long salida;
    private Long transferencia;

    public MovimientosAyerDTO(Long entrada, Long salida, Long transferencia) {
        this.entrada = entrada;
        this.salida = salida;
        this.transferencia = transferencia;
    }

    public Long getEntrada() {
        return entrada;
    }

    public Long getSalida() {
        return salida;
    }

    public Long getTransferencia() {
        return transferencia;
    }
}