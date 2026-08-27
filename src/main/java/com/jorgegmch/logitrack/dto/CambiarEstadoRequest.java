package com.jorgegmch.logitrack.dto;

import com.jorgegmch.logitrack.entity.enums.EstadoOrden;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoRequest {
    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado;
}