package com.jorgegmch.logitrack.dto;

import java.util.List;

import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovimientoRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipo;

    private Long bodegaOrigenId;

    private Long bodegaDestinoId;

    @NotEmpty(message = "Debe especificar al menos un producto")
    private List<Long> productoIds;

    @NotEmpty(message = "Debe especificar la cantidad de cada producto")
    private List<Integer> cantidades;
}