package com.jorgegmch.logitrack.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccionSugeridaDTO {
    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private Long ordenId;
    private Long productoId;
    private Long bodegaId;
}